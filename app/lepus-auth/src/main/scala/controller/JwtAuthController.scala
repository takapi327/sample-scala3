package controller

import javax.inject.{ Singleton, Inject }

import com.google.inject.Injector

import cats.effect.IO

import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.headers.Location
import org.http4s.implicits.*

import com.nimbusds.openid.connect.sdk.{ Nonce, OIDCTokenResponse, OIDCTokenResponseParser }
import com.nimbusds.oauth2.sdk.pkce.CodeVerifier

import lepus.app.jwt.{ JwtSettings, JwtHelper }
import lepus.guice.inject.Inject as LepusInject

import auth.*

@Singleton
class JwtAuthController @Inject() (
  jwt: JwtSettings
) extends JwtHelper(jwt):

  def login(codeVerifier: CodeVerifier, nonce: Nonce): IO[Response[IO]] =
    Found(Location(Uri.unsafeFromString(AuthenticationProvider.loginURL(nonce, codeVerifier))))

  def logout: IO[Response[IO]] =
    Found(Location(Uri.unsafeFromString(AuthenticationProvider.logoutURL)))
      .removeJwtCookies

  def callback(req: Request[IO], codeVerifier: CodeVerifier, nonce: Nonce): IO[Response[IO]] =
    for
      response <- IO(IdTokenProvider.request(codeVerifier)(using req).toHTTPRequest.send())
    yield
      val oidc = OIDCTokenResponseParser.parse(response)
      if oidc.indicatesSuccess() then
        val res = oidc.toSuccessResponse.asInstanceOf[OIDCTokenResponse]
        val result = IDTokenParser.validator.validate(res.getOIDCTokens.getIDToken, nonce)
        val cookie = JwtCookie.fromConfig(
          "userName" -> result.getStringClaim("name"),
          "userEmail" -> result.getStringClaim("email")
        )
        val response = Response[IO](status = Found, headers = Headers(Location(uri"hello")))
          .addCookie(cookie)

        println(response.headers.headers)

        response
      else
        Response(Unauthorized)

  def hello(req: Request[IO]): IO[Response[IO]] =
    val cookies = req.jwtCookies
    val result = (cookies.get("userName"), cookies.get("userEmail")) match
      case (Some(name), Some(email)) => s"User name: $name, email: $email"
      case _ => "なし"
    println(req.jwtCookies)
    Ok(result)

object JwtAuthController:
  def apply(): Injector ?=> JwtAuthController = LepusInject[JwtAuthController]

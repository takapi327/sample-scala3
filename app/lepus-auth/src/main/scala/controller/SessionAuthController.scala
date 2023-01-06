package controller

import org.typelevel.vault.Key

import cats.effect.IO

import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.headers.Location
import org.http4s.implicits.*

import com.nimbusds.openid.connect.sdk.{ Nonce, OIDCTokenResponse, OIDCTokenResponseParser }
import com.nimbusds.oauth2.sdk.pkce.CodeVerifier

import lepus.app.session.*
import lepus.app.syntax.*

import auth.*
import app.User

class SessionAuthController(codeVerifier: CodeVerifier, nonce: Nonce, userKey: Key[User]):

  def login: IO[Response[IO]] =
    //println("=============================")
    //println(AuthenticationProvider.loginURL)
    //println("=============================")
    SeeOther(Location(Uri.unsafeFromString(AuthenticationProvider.loginURL(nonce, codeVerifier))))

  def logout: IO[Response[IO]] =
    SeeOther(Location(Uri.unsafeFromString(AuthenticationProvider.logoutURL)))
      .withAttribute(SessionReset.key, SessionReset)

  def callback(req: Request[IO]): IO[Response[IO]] =
    for
      response <- IO(IdTokenProvider.request(codeVerifier)(using req).toHTTPRequest.send())
    yield
      //println(IdTokenProvider.request.toHTTPRequest.getURL.toString + "?" + IdTokenProvider.request.toHTTPRequest.getQuery)
      val oidc = OIDCTokenResponseParser.parse(response)
      if oidc.indicatesSuccess() then
        val res = oidc.toSuccessResponse.asInstanceOf[OIDCTokenResponse]
        println(s"ID Token:      ${res.getOIDCTokens.getIDToken.serialize()}")
        println(s"Access Token:  ${res.getOIDCTokens.getAccessToken.getValue}")
        println(s"Refresh Token: ${res.getOIDCTokens.getRefreshToken.getValue}")
        println("成功")

        val result = IDTokenParser.validator.validate(res.getOIDCTokens.getIDToken, nonce)
        println(result.toJWTClaimsSet.getClaims)
        println(result.getStringClaim("name"))
        println(result.getStringClaim("email"))
        println(result.getStringClaim("nickname"))
        Response(status = SeeOther, headers = Headers(Location(uri"hello")))
          .withAttribute(userKey, User(result.getStringClaim("name"), result.getStringClaim("email")))
      else
        val res = oidc.toErrorResponse
        println(s"Code:           ${res.getErrorObject.getCode}")
        println(s"Description:    ${res.getErrorObject.getDescription}")
        println(s"HTTPStatusCode: ${res.getErrorObject.getHTTPStatusCode}")
        println(s"URI:            ${res.getErrorObject.getURI}")
        Response(Unauthorized)

  def hello(req: Request[IO]): IO[Response[IO]] =
    Ok(
      req.attributes.lookup(userKey)
        .fold("なし")(user => s"User name: ${user.name}, email: ${user.email}")
    )
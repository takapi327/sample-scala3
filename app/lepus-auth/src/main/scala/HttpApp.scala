package app

import cats.effect.IO

import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.server.Router
import org.http4s.headers.Location
import org.http4s.implicits.*

import lepus.app.LepusApp
import lepus.logger.{ LoggerF, LoggingIO, given }

import com.nimbusds.openid.connect.sdk.{ Nonce, OIDCTokenResponse, OIDCTokenResponseParser }
import com.nimbusds.oauth2.sdk.pkce.CodeVerifier

import auth.*

object HttpApp extends LepusApp[IO], LoggingIO:

  val codeVerifier = new CodeVerifier()
  val nonce = new Nonce()

  private val authRoutes = HttpRoutes.of[IO] {
    case GET -> Root / "login" =>
      //println("=============================")
      //println(AuthenticationProvider.loginURL)
      //println("=============================")
      IO(Response(status = SeeOther, headers = Headers(Location(Uri.unsafeFromString(AuthenticationProvider.loginURL(nonce, codeVerifier))))))
    case GET -> Root / "logout" =>
      IO(Response(status = SeeOther, headers = Headers(Location(Uri.unsafeFromString(AuthenticationProvider.logoutURL)))))
    case req@GET -> Root / "callback" =>
      for
        response <- IO(IdTokenProvider.request(codeVerifier)(using req).toHTTPRequest.send())
      yield
        //println(IdTokenProvider.request.toHTTPRequest.getURL.toString + "?" + IdTokenProvider.request.toHTTPRequest.getQuery)
        val oidc     = OIDCTokenResponseParser.parse(response)
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
        else
          val res = oidc.toErrorResponse
          println(s"Code:           ${res.getErrorObject.getCode}")
          println(s"Description:    ${res.getErrorObject.getDescription}")
          println(s"HTTPStatusCode: ${res.getErrorObject.getHTTPStatusCode}")
          println(s"URI:            ${res.getErrorObject.getURI}")

        println(req.uri)

        Response(status = SeeOther, headers = Headers(Location(uri"hello")))
    case GET -> Root / "hello" => Ok("Hello")
  }

  override val router = Router(
    "/" -> authRoutes
  ).orNotFound

  override val errorHandler: PartialFunction[Throwable, IO[Response[IO]]] =
    case error: Throwable => logger.error(s"Unexpected error: $error", error)
      .as(Response(Status.InternalServerError))

package app

import cats.effect.{ IO, Resource }

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

import cats.syntax.all.*
import cats.effect.*
import org.http4s.ember.server.EmberServerBuilder
import com.comcast.ip4s.*

import org.typelevel.vault.{ Key, Vault }
import com.google.inject.{ Guice, Injector, AbstractModule, TypeLiteral }

import session.*

object Example extends ResourceApp.Forever, LoggingIO:

  val codeVerifier = new CodeVerifier()
  val nonce = new Nonce()

  private val userKey = SessionKey[User]

  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "login" =>
      SeeOther(Location(Uri.unsafeFromString(AuthenticationProvider.loginURL(nonce, codeVerifier))))
    case GET -> Root / "logout" =>
      SeeOther(Location(Uri.unsafeFromString(AuthenticationProvider.logoutURL)))
        .map(_.withAttribute(VaultSessionMiddleware.VaultSessionReset.key, VaultSessionMiddleware.VaultSessionReset))
    case req @ GET -> Root / "callback" => Controller.callback(req)
    case req @ GET -> Root / "hello" => Controller.hello(req)
  }

  object Controller:

    def callback(req: Request[IO]): IO[Response[IO]] =
      for
        response <- IO(IdTokenProvider.request(codeVerifier)(using req).toHTTPRequest.send())
      yield
        val oidc = OIDCTokenResponseParser.parse(response)
        if oidc.indicatesSuccess() then
          val res = oidc.toSuccessResponse.asInstanceOf[OIDCTokenResponse]
          val result = IDTokenParser.validator.validate(res.getOIDCTokens.getIDToken, nonce)
          Response(status = SeeOther, headers = Headers(Location(uri"hello")))
            .withAttribute(userKey, User(result.getStringClaim("name"), result.getStringClaim("email")))
        else
          //val res = oidc.toErrorResponse
          Response(Unauthorized)

    def hello(req: Request[IO]): IO[Response[IO]] =
      Ok(
        req.attributes.lookup(userKey).fold("なし")(user => s"User name: ${user.name}, email: ${user.email}")
      )

  case class User(name: String, email: String)

  override def run(args: List[String]): Resource[IO, Unit] =
    for
      storage <- Resource.eval(SessionStorage.create[IO, Vault]())
      _ <- EmberServerBuilder.default[IO]
             .withPort(port"5555")
             .withHttpApp(
               VaultSessionMiddleware.impl(storage, secure = false)(routes).orNotFound
             )
             .withErrorHandler({
               case error: Throwable => logger.error(s"Unexpected error: $error", error)
                 .as(Response(Status.InternalServerError))
             })
             .build
    yield ()

object SessionKey:
  def apply[T]: Key[T] = Key.newKey[cats.effect.SyncIO, T].unsafeRunSync()

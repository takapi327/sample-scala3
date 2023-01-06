package app

import cats.effect.{ IO, Resource }

import com.google.inject.Injector

import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.server.Router
import org.http4s.headers.Location
import org.http4s.implicits.*

import lepus.app.LepusApp
import lepus.logger.{ LoggerF, LoggingIO, given }
import lepus.app.session.*
import lepus.app.syntax.*

import com.nimbusds.openid.connect.sdk.{ Nonce, OIDCTokenResponse, OIDCTokenResponseParser }
import com.nimbusds.oauth2.sdk.pkce.CodeVerifier

import auth.*
import controller.{ SessionAuthController, JwtAuthController }

case class User(name: String, email: String)

object HttpApp extends LepusApp[IO], LoggingIO:

  val codeVerifier = new CodeVerifier()
  val nonce = new Nonce()
  private val userKey = SessionKey[User]

  private val sessionAuthController: SessionAuthController =
    new SessionAuthController(codeVerifier, nonce, userKey)

  private val sessionAuthRoutes = HttpRoutes.of[IO] {
    case GET -> Root / "login"  => sessionAuthController.login
    case GET -> Root / "logout" => sessionAuthController.logout
    case req@GET -> Root / "callback" => sessionAuthController.callback(req)
    case req@GET -> Root / "hello"    => sessionAuthController.hello(req)
  }

  private val jwtAuthRoutes: Injector ?=> HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "login" => JwtAuthController().login(codeVerifier, nonce)
    case GET -> Root / "logout" => JwtAuthController().logout
    case req@GET -> Root / "callback" => JwtAuthController().callback(req, codeVerifier, nonce)
    case req@GET -> Root / "hello" => JwtAuthController().hello(req)
  }

  override val router = Router(
    "/" -> sessionAuthRoutes
  )

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
        .withAttribute(VaultSessionMiddleware.VaultSessionReset.key, VaultSessionMiddleware.VaultSessionReset)
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

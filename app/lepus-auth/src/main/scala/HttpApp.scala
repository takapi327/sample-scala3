package app

import java.net.{ URI, URL }

import cats.effect.IO

import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.server.Router
import org.http4s.headers.Location
import org.http4s.implicits.*

import lepus.app.LepusApp
import lepus.logger.{LoggerF, LoggingIO, given}

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.util.DefaultResourceRetriever
import com.nimbusds.openid.connect.sdk.validators.IDTokenValidator
import com.nimbusds.oauth2.sdk.id.{ClientID, Issuer, State}
import com.nimbusds.oauth2.sdk.auth.{ClientSecretBasic, Secret}
import com.nimbusds.openid.connect.sdk.{ Nonce, Prompt, AuthenticationRequest, OIDCTokenResponse, OIDCTokenResponseParser }

object HttpApp extends LepusApp[IO], LoggingIO:

  private val authRoutes = HttpRoutes.of[IO] {
    case GET -> Root / "login" =>
      //println("=============================")
      //println(AuthenticationProvider.loginURL)
      //println("=============================")
      IO(Response(status = SeeOther, headers = Headers(Location(Uri.unsafeFromString(AuthenticationProvider.loginURL)))))
    case GET -> Root / "logout" =>
      IO(Response(status = SeeOther, headers = Headers(Location(Uri.unsafeFromString(AuthenticationProvider.logoutURL)))))
    case req@GET -> Root / "callback" =>
      for
        response <- IO(IdTokenProvider.request(using req).toHTTPRequest.send())
      yield
        //println(IdTokenProvider.request.toHTTPRequest.getURL.toString + "?" + IdTokenProvider.request.toHTTPRequest.getQuery)
        val oidc     = OIDCTokenResponseParser.parse(response)
        if oidc.indicatesSuccess() then
          val res = oidc.toSuccessResponse.asInstanceOf[OIDCTokenResponse]
          println(s"ID Token:      ${res.getOIDCTokens.getIDToken.serialize()}")
          println(s"Access Token:  ${res.getOIDCTokens.getAccessToken.getValue}")
          println(s"Refresh Token: ${res.getOIDCTokens.getRefreshToken.getValue}")
          println("成功")

          val issuer    = new Issuer("https://dev-nw1kyvpiev7up1bq.us.auth0.com/")
          val clientID  = new ClientID(AuthenticationProvider.clientId)
          val jwkSetURL = new URL("https://dev-nw1kyvpiev7up1bq.us.auth0.com/.well-known/jwks.json")

          val validator = new IDTokenValidator(issuer, clientID, JWSAlgorithm.RS256, jwkSetURL, new DefaultResourceRetriever(2000, 2000))

          val result = validator.validate(res.getOIDCTokens.getIDToken, nonce)
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

import lepus.core.util.Configuration

import com.nimbusds.oauth2.sdk.{ ResponseType, Scope }
import com.nimbusds.oauth2.sdk.pkce.{ CodeVerifier, CodeChallengeMethod }

val codeVerifier = new CodeVerifier()
val nonce        = new Nonce()

object AuthenticationProvider:

  private val config = Configuration.load()

  private val domain   = config.get[String]("com.auth0.domain")
  val clientId = config.get[String]("com.auth0.clientId")

  private val authenticationEndpoint = config.get[String]("com.auth0.authentication.endpoint")
  private val authenticationScopes   = config.get[Seq[String]]("com.auth0.authentication.scopes")

  private val base: AuthenticationRequest.Builder = new AuthenticationRequest.Builder(
    new ResponseType("code"),
    authenticationScopes.foldLeft(new Scope("openid"))((acc, scope) => {
      acc.add(scope)
      acc
    }),
    new ClientID(clientId),
    new URI("http://localhost:5555/callback")
  ).endpointURI(new URI(authenticationEndpoint))
    .state(new State())
    .nonce(nonce)
    .codeChallenge(codeVerifier, CodeChallengeMethod.S256)

  val loginURL = base.build().toURI.toASCIIString
  val logoutURL = s"https://$domain/v2/logout?client_id=$clientId&returnTo=http://localhost:5555/login"

import com.nimbusds.oauth2.sdk.{ AuthorizationCode, AuthorizationCodeGrant, TokenRequest }
import com.nimbusds.openid.connect.sdk.{ AuthenticationSuccessResponse, AuthenticationErrorResponse, AuthenticationResponseParser }

object IdTokenProvider:

  private val config = Configuration.load()

  private val tokenEndpoint = config.get[String]("com.auth0.token.endpoint")
  private val clientId      = config.get[String]("com.auth0.clientId")
  private val clientSecret  = config.get[String]("com.auth0.clientSecret")

  private def response(using request: Request[IO]): AuthenticationSuccessResponse | AuthenticationErrorResponse =
    val respParser = AuthenticationResponseParser.parse(new URI("http://" + request.uri))
    if respParser.indicatesSuccess() then respParser.toSuccessResponse else respParser.toErrorResponse

  private val validateCallback: Request[IO] ?=> String = response match
    case r: AuthenticationErrorResponse   => r.getErrorObject.getCode
    case r: AuthenticationSuccessResponse => r.getAuthorizationCode.getValue

  private val clientSecretBasic = new ClientSecretBasic(new ClientID(clientId), new Secret(clientSecret))
  private val codeGrant: Request[IO] ?=> AuthorizationCodeGrant =
    new AuthorizationCodeGrant(new AuthorizationCode(validateCallback), new URI("http://localhost:5555/callback"), new CodeVerifier(codeVerifier.getValue))

  val request: Request[IO] ?=> TokenRequest = new TokenRequest(new URI(tokenEndpoint), clientSecretBasic, codeGrant)
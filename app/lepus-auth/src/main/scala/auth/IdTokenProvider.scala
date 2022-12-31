package auth

import java.net.URI

import cats.effect.IO

import org.http4s.Request

import com.nimbusds.oauth2.sdk.{ AuthorizationCode, AuthorizationCodeGrant, TokenRequest }
import com.nimbusds.oauth2.sdk.id.ClientID
import com.nimbusds.oauth2.sdk.auth.{ ClientSecretBasic, Secret }
import com.nimbusds.oauth2.sdk.pkce.CodeVerifier
import com.nimbusds.openid.connect.sdk.{ AuthenticationSuccessResponse, AuthenticationErrorResponse, AuthenticationResponseParser }

object IdTokenProvider extends Auth0ConfigReader:

  private def response(using request: Request[IO]): AuthenticationSuccessResponse | AuthenticationErrorResponse =
    val respParser = AuthenticationResponseParser.parse(new URI("http://" + request.uri))
    if respParser.indicatesSuccess() then respParser.toSuccessResponse else respParser.toErrorResponse

  private val validateCallback: Request[IO] ?=> String = response match
    case r: AuthenticationErrorResponse   => r.getErrorObject.getCode
    case r: AuthenticationSuccessResponse => r.getAuthorizationCode.getValue

  private val clientSecretBasic = new ClientSecretBasic(new ClientID(clientId), new Secret(clientSecret))
  private def codeGrant(codeVerifier: CodeVerifier): Request[IO] ?=> AuthorizationCodeGrant =
    new AuthorizationCodeGrant(new AuthorizationCode(validateCallback), new URI("http://localhost:5555/callback"), new CodeVerifier(codeVerifier.getValue))

  def request(codeVerifier: CodeVerifier): Request[IO] ?=> TokenRequest = new TokenRequest(new URI(tokenEndpoint), clientSecretBasic, codeGrant(codeVerifier))

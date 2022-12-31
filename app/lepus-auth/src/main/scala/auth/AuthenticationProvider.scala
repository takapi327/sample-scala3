package auth

import java.net.URI

import com.nimbusds.oauth2.sdk.pkce.{ CodeVerifier, CodeChallengeMethod }
import com.nimbusds.oauth2.sdk.{ ResponseType, Scope }
import com.nimbusds.oauth2.sdk.id.{ ClientID, State }
import com.nimbusds.openid.connect.sdk.{ Nonce, AuthenticationRequest }

object AuthenticationProvider extends Auth0ConfigReader:

  private def base(nonce: Nonce, codeVerifier: CodeVerifier): AuthenticationRequest.Builder = new AuthenticationRequest.Builder(
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

  def loginURL(nonce: Nonce, codeVerifier: CodeVerifier) = base(nonce, codeVerifier).build().toURI.toASCIIString
  val logoutURL = s"https://$domain/v2/logout?client_id=$clientId&returnTo=http://localhost:5555/login"

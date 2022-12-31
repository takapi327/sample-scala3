package auth

import java.net.URL

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.util.DefaultResourceRetriever
import com.nimbusds.oauth2.sdk.id.{ ClientID, Issuer }
import com.nimbusds.openid.connect.sdk.validators.IDTokenValidator

object IDTokenParser extends Auth0ConfigReader:

  private val issuer    = new Issuer("https://dev-nw1kyvpiev7up1bq.us.auth0.com/")
  private val clientID  = new ClientID(clientId)
  private val jwkSetURL = new URL("https://dev-nw1kyvpiev7up1bq.us.auth0.com/.well-known/jwks.json")

  val validator = new IDTokenValidator(issuer, clientID, JWSAlgorithm.RS256, jwkSetURL, new DefaultResourceRetriever(2000, 2000))

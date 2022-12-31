package auth

import lepus.core.util.Configuration

trait Auth0ConfigReader:

  private val config = Configuration.load()

  val domain                 = config.get[String]("com.auth0.domain")
  val clientId               = config.get[String]("com.auth0.clientId")
  val clientSecret           = config.get[String]("com.auth0.clientSecret")
  val authenticationEndpoint = config.get[String]("com.auth0.authentication.endpoint")
  val authenticationScopes   = config.get[Seq[String]]("com.auth0.authentication.scopes")
  val tokenEndpoint          = config.get[String]("com.auth0.token.endpoint")

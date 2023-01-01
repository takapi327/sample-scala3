package session

import java.util.Base64

import cats.Functor
import cats.syntax.all.*

import cats.effect.std.Random

import org.http4s.Request

case class SessionIdentifier(value: String)

object SessionIdentifier:

  private val base64 = Base64.getEncoder

  def create[F[_]: Functor](random: Random[F], numBytes: Int): F[SessionIdentifier] =
    random.nextBytes(numBytes).map(v => SessionIdentifier(base64.encodeToString(v)))

  def extract[F[_]](request: Request[F], sessionIdentifierName: String): Option[SessionIdentifier] =
    request.cookies.find(_.name === sessionIdentifierName).map(v => SessionIdentifier(v.content))

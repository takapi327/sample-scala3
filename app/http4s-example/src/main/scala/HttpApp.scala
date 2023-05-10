
import org.typelevel.vault.Key

import cats.effect.*
import cats.effect.kernel.Unique

import com.comcast.ip4s.*

import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.server.{ Router, ServerRequestKeys }
import org.http4s.implicits.*
import org.http4s.ember.server.EmberServerBuilder

object HttpApp extends ResourceApp.Forever:

  private val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "healthcheck" => Ok("Healthcheck Ok")
    case GET -> Root => Ok("Hello World!")
  }

  override def run(args: List[String]): Resource[IO, Unit] =
    for
      _ <- EmberServerBuilder.default[IO]
        .withPort(port"9000")
        .withHttpApp(routes.orNotFound)
        .build
    yield ()

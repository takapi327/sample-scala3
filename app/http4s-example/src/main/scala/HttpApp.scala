
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
    case req@GET -> Root / "login" =>
      println("======================")
      println(req.attributes.isEmpty)
      println(req.attributes.empty.isEmpty)
      println(req.attributes.m)
      println(req.attributes.m(req.attributes.m.keys.head).a)
      println(req.attributes.lookup(Key.newKey[SyncIO, Unique.Token].unsafeRunSync()))
      println(req.attributes.lookup(Message.Keys.TrailerHeaders[IO]))
      println(req.attributes.lookup(ServerRequestKeys.SecureSession))
      println(req.attributes.lookup(Request.Keys.ConnectionInfo))
      println(req.cookies.map(_.name))
      println("======================")
      Ok("")
  }

  override def run(args: List[String]): Resource[IO, Unit] =
    for
      _ <- EmberServerBuilder.default[IO]
        .withPort(port"5555")
        .withHttpApp(routes.orNotFound)
        .build
    yield ()
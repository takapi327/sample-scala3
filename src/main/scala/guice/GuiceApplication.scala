package guice

import javax.inject.{ Inject, Singleton }

import com.google.inject.Injector

import com.comcast.ip4s.*

import cats.effect.*

import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.server.{ Server, Router }
import org.http4s.ember.server.EmberServerBuilder

@Singleton
class Controller @Inject()(database: module.Database):
  val ok: IO[Response[IO]] =
    Ok(database.name)

@Singleton
class GuiceApplication:

  import scala.deriving.Mirror
  import scala.reflect.ClassTag
  def inject[T](clazz: Class[T])(using injector: Injector): T =
    injector.getInstance[T](clazz)
  def inject[T: ClassTag](using Injector): T =
    inject[T](summon[ClassTag[T]].runtimeClass.asInstanceOf[Class[T]])

  def app(using Injector) = Router(
    "/" -> HttpRoutes.of[IO] {
      case GET -> Root => inject[Controller].ok
    }
  ).orNotFound

  def start(injector: Injector): Resource[IO, Server] =
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"9000")
      .withHttpApp(app(using injector))
      .withErrorHandler(error => {
        println(error.getMessage)
        error.printStackTrace()
        Ok("")
      })
      .build

package guice

import javax.inject.{ Inject, Singleton }

import com.google.inject.Injector

import com.comcast.ip4s.*

import cats.effect.*

import doobie.*
import doobie.implicits.*
import doobie.hikari.HikariTransactor

import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.server.{ Server, Router }
import org.http4s.ember.server.EmberServerBuilder

import guice.model.Connection

case class Task(
  id:          Option[Long]   = None,
  title:       String,
  description: Option[String] = None
)

object Task

@Singleton
class Repository @Inject()(connection: Connection):
  def getAll =
    sql"select id, title, description from todo_task".query[Task].to[List].transact[IO](connection.xa)

@Singleton
class Controller @Inject()(database: module.Database, repository: Repository):
  val ok: IO[Response[IO]] =
    for
      todo <- repository.getAll
      res  <- Ok(database.name + todo.toString)
    yield res
object Controller:
  def apply(): Injector ?=> Controller = inject[Controller]

import scala.reflect.ClassTag
def inject[T](clazz: Class[T])(using injector: Injector): T =
  injector.getInstance[T](clazz)
def inject[T: ClassTag](using Injector): T =
  inject[T](summon[ClassTag[T]].runtimeClass.asInstanceOf[Class[T]])

object GuiceApplication:

  def app(using Injector) = Router(
    "/" -> HttpRoutes.of[IO] {
      case GET -> Root => Controller().ok
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

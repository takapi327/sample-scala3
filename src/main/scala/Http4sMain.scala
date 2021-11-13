
import scala.util.Try
import scala.concurrent.ExecutionContext.Implicits.global

import cats.effect.*
import cats.syntax.all.*

import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.blaze.server.*
import org.http4s.implicits.*
import org.http4s.server.Router

import library.util.Configuration

import interfaceAdapter.gateway.repository.*

object personController:
  val personRepository = PersonRepository()
  
  def getPerson(id: Long): IO[Response[IO]] =
    for
      person <- personRepository.getById(id)
      res    <- Ok(s"id: ${person._1}, Name: ${person._2}, Age: ${person._3}")
    yield
      res

object LongVar:
  def unapply(str: String): Option[Long] =
    if (!str.isEmpty) then Try(str.toLong).toOption else None

val baseService = HttpRoutes.of[IO] {
  case GET -> Root / "hello" / name => Ok(s"Hello, $name.")
}

val personService = HttpRoutes.of[IO] {
  case GET -> Root / "person" / LongVar(id)  => personController.getPerson(id)
}

val apiServices = baseService <+> personService

val httpApp = Router(
  "/"    -> baseService,
  "/api" -> apiServices
).orNotFound

//given cs: ContextShift[IO] = IO.contextShift(global)
//given timer: Timer[IO] = IO.timer(global)

object Http4sMain extends IOApp:

  private lazy val config = Configuration()

  private lazy val host: String = config.get[String]("http.host")
  private lazy val port: Int    = config.get[Int]("http.port")
  
  def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO]
      .bindHttp(port, host)
      .withHttpApp(httpApp)
      .withExecutionContext(global)
      .withoutBanner
      .serve
      .compile
      .drain
      .as(ExitCode.Success)

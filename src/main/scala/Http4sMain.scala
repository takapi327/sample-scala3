
import scala.util.Try
import scala.concurrent.duration.*
import scala.concurrent.ExecutionContext.Implicits.global

import cats.effect.*
import cats.syntax.all.*

import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.blaze.server.*
import org.http4s.implicits.*
import org.http4s.headers.Origin
import org.http4s.server.Router
import org.http4s.server.middleware.*

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

val corsOriginService = CORS.policy
  .withAllowOriginHost(Set(
    Origin.Host(Uri.Scheme.http,  Uri.RegName("localhost"), None),
    Origin.Host(Uri.Scheme.https, Uri.RegName("localhost"), None)
  ))
  .withAllowCredentials(false)
  .withMaxAge(1.seconds)
  .apply(baseService)

def static(file: String, request: Request[IO]): IO[Response[IO]] = {
  StaticFile.fromString("/Users/takapi327/development/sample-scala3/" + file, Some(request)).getOrElseF(NotFound())
}

val staticService = HttpRoutes.of[IO] {
  case request @ GET -> Root / path if List(".html", ".css", ".js")
    .exists(path.endsWith) => static(path, request)
}

def requestToResponse(request: Request[IO]): IO[Response[IO]] =
  ->.unapply(request) match
    case Some((method, path1)) if method.name == "GET" => /.unapply(path1) match
      case Some((path2, str)) => /.unapply(path2) match
        case Some((_, str1)) if str1 == "hello" => Ok(s"Hello, $str")
        case _ => NotFound("")
      case None => NotFound("")
end requestToResponse

val testService = HttpRoutes.of[IO] {
  case request => requestToResponse(request)
}

val httpApp = Router(
  "/"     -> testService,//staticService,
  "/cors" -> corsOriginService,
  "/api"  -> apiServices
).orNotFound

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

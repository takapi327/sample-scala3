package guice

import com.google.inject.{ AbstractModule, Guice, Injector }

import cats.effect.*

import guice.model.Connection
import guice.module.DatabaseModule

object DatabaseModule extends DatabaseModule:
  override val driver:   String = "com.mysql.cj.jdbc.Driver"
  override val jdbcURL:  String = "jdbc:mysql://127.0.0.1:53306/edu_todo"
  override val user:     String = "lepus"
  override val password: String = "docker"

object Main extends ResourceApp.Forever:
  /*
  def modules(connection: Connection) = new AbstractModule:
    override def configure(): Unit =
      bind(classOf[module.DatabaseProvider])
      bind(classOf[Connection]).toInstance(connection)
  */
  val modules = new AbstractModule:
    override def configure(): Unit =
      bind(classOf[module.DatabaseProvider])

  //val injector: Injector = Guice.createInjector(modules)
  //val instance: GuiceApplication = injector.getInstance(classOf[GuiceApplication])

  //val default = Resource.eval(Sync[IO].delay(Map.empty[String, HikariTransactor[IO]]))

  def run(args: List[String]): Resource[IO, Unit] =
    println("Start")
    for
      module <- DatabaseModule.build
      injector: Injector = Guice.createInjector(Seq(modules, module): _*)
      _  <- GuiceApplication.start(injector)
    yield ()

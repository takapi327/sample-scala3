package guice

import javax.inject.Provider

import scala.reflect.ClassTag

import com.google.inject.{ AbstractModule, Guice, Injector }

import cats.effect.*

import doobie.ExecutionContexts
import doobie.hikari.HikariTransactor

class Connection(val xa: HikariTransactor[IO])

trait ResourceModule[T: ClassTag]:

  type InjectType = T

  def resource: Resource[IO, T]

  val build: Resource[IO, AbstractModule] =
    resource.map(v =>
      new AbstractModule:
        override def configure(): Unit =
          bind(summon[ClassTag[T]].runtimeClass.asInstanceOf[Class[T]]).toInstance(v)
    )

object DatabaseModule extends ResourceModule[Connection]:
  override def resource: Resource[IO, Connection] =
    for
      ec <- ExecutionContexts.fixedThreadPool[IO](32)
      xa <- HikariTransactor.newHikariTransactor[IO](
              "com.mysql.cj.jdbc.Driver",
              "jdbc:mysql://127.0.0.1:53306/edu_todo",
              "lepus",
              "docker",
              ec
            )
      _ = println("=========Start==========")
    yield new Connection(xa)

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
    for
      module <- DatabaseModule.build
      //ec <- ExecutionContexts.fixedThreadPool[IO](32)
      //xa <- HikariTransactor.newHikariTransactor[IO](
      //        "com.mysql.cj.jdbc.Driver",          // driver classname
      //        "jdbc:mysql://127.0.0.1:43306/edu_todo", // connect URL (driver-specific)
      //        "nextbeat",                          // user
      //        "docker",                            // password
      //        ec                                   // await connection here
      //      )
      //provider = new Provider[HikariTransactor[IO]]:
      //         lazy val get: HikariTransactor[IO] = xa
      injector: Injector = Guice.createInjector(Seq(modules, module): _*)
      _  <- GuiceApplication.start(injector)
    yield ()

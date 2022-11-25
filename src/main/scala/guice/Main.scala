package guice

import com.google.inject.{ AbstractModule, Guice, Injector }

import cats.effect.*

object Main extends ResourceApp.Forever:
  val modules = new AbstractModule:
    override def configure(): Unit =
      bind(classOf[module.DatabaseProvider])

  val injector: Injector         = Guice.createInjector(modules)
  val instance: GuiceApplication = injector.getInstance(classOf[GuiceApplication])

  def run(args: List[String]): Resource[IO, Unit] =
    instance.start(injector).map(_ => ())

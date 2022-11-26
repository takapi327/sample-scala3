package guice.module

import scala.reflect.ClassTag

import com.google.inject.AbstractModule

import cats.effect.{ IO, Resource }

trait ResourceModule[T: ClassTag]:

  protected val resource: Resource[IO, T]

  lazy val build: Resource[IO, AbstractModule] =
    resource.map(v =>
      new AbstractModule:
        override def configure(): Unit =
          bind(summon[ClassTag[T]].runtimeClass.asInstanceOf[Class[T]]).toInstance(v)
    )

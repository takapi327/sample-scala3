package library.rdb

import java.lang.reflect.Constructor

import scala.reflect.ClassTag

import com.google.inject.{ Module => GuiceModule }

import library.util.Configuration

trait MixinRepository:
  lazy val config = Configuration()

  lazy val libraryModule = config.get[String]("library.rdb.module")

  //val classLoader = ClassLoader.getSystemClassLoader()
  //val clazz       = classLoader.loadClass(libraryModule).asInstanceOf[Class[T]]
  //println(clazz)
  //println(classOf[test].isInstanceOf[clazz.type])
  //println(classOf[test].asInstanceOf[clazz.type])
  //val hoge = classOf[test].asInstanceOf[clazz.type].getDeclaredConstructor()
  //hoge.setAccessible(true)
  //hoge.newInstance(new Object())
  //println(c.newInstance)
  //println(Class.forName(libraryModule))
  //val c: Class[T] = Class.forName(libraryModule, true, tag.runtimeClass.getClassLoader).asInstanceOf[Class[T]]
  val classType: Class[_] = Class.forName(libraryModule)//.asInstanceOf[Class[T]]
  //println(c.getDeclaredConstructor())
  //println(c.getDeclaredConstructor().newInstance())
  val constructor = classType.getDeclaredConstructor()
  //constructor.setAccessible(true)
  val instance = constructor.newInstance()
  println(classType)
  val lib: DoobieRepository[cats.effect.IO] = instance match {
    case a: hoge  => hoge()
    case a: test  => test()
    case _        => throw new IllegalArgumentException
  }
  import doobie.*
  import doobie.implicits.*

  import cats.data.*
  import cats.effect.*
  import cats.implicits.*
  import cats.effect.unsafe.implicits.global

  def filterbyName(pat: String): List[(String, String)] =
    lib.transactor.use {
      sql"select name, code from country where name like $pat"
        .query[(String, String)]
        .to[List]
        .transact[IO]
    }.unsafeRunSync()
  println(filterbyName("N%"))

  //println(c.getField("MODULE$").get(null).asInstanceOf[Function0[test]])
  //println(hoge)
  //println(classOf[T].isAssignableFrom(clazz))
  //println(classOf[T].isAssignableFrom(clazz))
  //val bindingClass: Class[_ <: PersonRepository] = clazz.asSubclass(classOf[PersonRepository])
  //val test: PersonRepository = classLoader.loadClass(module).asInstanceOf[Class[PersonRepository]]
  //println(clazz.getConstructor(clazz.getClass))
  //println(clazz.getConstructor())
  /*
  classOf[T].isAssignableFrom(clazz) match
    case true  => println("OK")
    case false => println("NO")
   */
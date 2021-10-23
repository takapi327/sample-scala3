
import scala.concurrent.ExecutionContext

import cats.data.*
import cats.implicits.*
import cats.effect.unsafe.implicits.global

import com.google.inject.{ Module => GuiceModule }

import library.rdb.*
import library.util.Configuration

import interfaceAdapter.gateway.repository.*

@main def DoobieMain: Unit =
  given ec: ExecutionContext = ExecutionContext.global

  val countryRepository = new CountryRepository()
  val personRepository  = new PersonRepository()

  /*
  val test1 = personRepository.byName("N%")
  val test2 = personRepository.byName2("N%")
  val test3 = personRepository.run()
  val test4 = countryRepository.biggerThan(2)
  val test5 = countryRepository.populationIn(1 to 3)
  println(test1)
  println(test2)
  println(test3)
  println(test4)
  println(test5)
  println(countryRepository.biggerThan(2, 1))
   */

  import java.lang.reflect.Constructor

  val config = Configuration()

  val module = config.get[String]("library.rdb.module")

  val classLoader = ClassLoader.getSystemClassLoader()
  val clazz = classLoader.loadClass(module).asInstanceOf[Class[Any]]
  println(clazz)
  println(classOf[CountryRepository].isAssignableFrom(clazz))
  println(classOf[PersonRepository].isAssignableFrom(clazz))
  //val bindingClass: Class[_ <: PersonRepository] = clazz.asSubclass(classOf[PersonRepository])
  //val test: PersonRepository = classLoader.loadClass(module).asInstanceOf[Class[PersonRepository]]
  println(classLoader.loadClass(module).asInstanceOf[Class[PersonRepository]])
  //println(clazz.getConstructor(clazz.getClass))
  //println(clazz.getConstructor())
  clazz match {
    case module: GuiceModule => println(module)
  }
  classOf[CountryRepository].isAssignableFrom(clazz) match
    case true  => println("OK")
    case false => println("NO")

  def constructModule(loadModuleClass: () => Class[T]): T =
    val moduleClass = loadModuleClass()
  /*
  println(personRepository.byName("N%"))
  println(personRepository.byName2("N%"))
  println(personRepository.run())

  println(countryRepository.biggerThan(2))
  println(countryRepository.populationIn(1 to 3))
   */
  /*
  println(io.unsafeRunSync())
  println(io2.unsafeRunSync())
  println(io3.unsafeRunSync())
  io4.unsafeRunSync().take(5).foreach(println)
  io5.unsafeRunSync().foreach(println)
  println(program6.quick.unsafeRunSync())
  println(program7.quick.unsafeRunSync())
  println(program8.quick.unsafeRunSync())
  println(program9.quick.unsafeRunSync())
  println(program9.compile.toList.map(_.toMap).quick.unsafeRunSync())
  program10.take(5).compile.toVector.unsafeRunSync().foreach(println)
  println(biggerThan(2).quick.unsafeRunSync())
  println(populationIn(1 to 3).quick.unsafeRunSync())
  println(populationIn(1 to 3, NonEmptyList.of("USA", "BRA", "PAK", "GBR")).quick.unsafeRunSync())
  //println(biggerThan2(0).check.unsafeRunSync())
  //println(biggerThan2(0).checkOutput.unsafeRunSync())
  //insert1("Alice", Some(12)).quick.unsafeRunSync()
  //insert1("Bob", None).quick.unsafeRunSync()
  //program12.quick.unsafeRunSync()
  //println(program11.quick.unsafeRunSync())
  //insert2("Jimmy", Some(42)).quick.unsafeRunSync()
  //val data = List[PersonInfo](("Alice", Some(12)), ("Jimmy", Some(42)), ("Frank", Some(12)), ("Daddy", None))
  //insertMany(data).quick.unsafeRunSync()
  try
    insert1("Alice", Some(12)).quick.unsafeRunSync()
  catch
    case e: java.sql.SQLException =>
      println(e.getMessage)
      println(e.getSQLState)
  safeInsert("Alice").quick.unsafeRunSync()
  byName("N%").unsafeRunSync()
  byName2("U%").unsafeRunSync()
  */
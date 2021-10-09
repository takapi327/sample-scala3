
import cats.data.*
import cats.implicits.*
import cats.effect.unsafe.implicits.global

import library.rds.*
import y.*

@main def DoobieMain: Unit =
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
  println(biggerThan2(0).check.unsafeRunSync())
  println(biggerThan2(0).checkOutput.unsafeRunSync())
  //insert1("Alice", Some(12)).quick.unsafeRunSync()
  //insert1("Bob", None).quick.unsafeRunSync()
  //program12.quick.unsafeRunSync()
  //println(program11.quick.unsafeRunSync())
  //insert2("Jimmy", Some(42)).quick.unsafeRunSync()
  val data = List[PersonInfo](("Alice", Some(12)), ("Jimmy", Some(42)), ("Frank", Some(12)), ("Daddy", None))
  //insertMany(data).quick.unsafeRunSync()
  try
    insert1("Alice", Some(12)).quick.unsafeRunSync()
  catch
    case e: java.sql.SQLException =>
      println(e.getMessage)
      println(e.getSQLState)
  safeInsert("Alice").quick.unsafeRunSync()
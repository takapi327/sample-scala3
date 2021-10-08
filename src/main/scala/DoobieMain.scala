
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
  println(biggerThan(150000000).quick.unsafeRunSync())
  println(populationIn(150000000 to 200000000).quick.unsafeRunSync())
  println(populationIn(100000000 to 300000000, NonEmptyList.of("USA", "BRA", "PAK", "GBR")).quick.unsafeRunSync())
  println(biggerThan(0).check.unsafeRunSync())
  println(biggerThan(0).checkOutput.unsafeRunSync())

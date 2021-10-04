
import cats.implicits.*
import cats.effect.unsafe.implicits.global

import library.rds.*

@main def DoobieMain: Unit =
  println(io.unsafeRunSync())
  println(io2.unsafeRunSync())
  println(io3.unsafeRunSync())
  io4.unsafeRunSync().take(5).foreach(println)
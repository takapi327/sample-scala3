package essentialEffect

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

@main def futureCancel: Unit =
  val a = Future {
    while ({
      Thread.sleep(100)
      true
    }) {}
    "a"
  }
  val b = Future {
    Thread.sleep(100)
    println("b")
    "b"
  }

  Future.firstCompletedOf(Seq(a, b)).foreach(println)

import cats.effect.IO
import cats.effect.unsafe.implicits.global

@main def ioCancel: Unit =
  val a = IO.interruptible {
    while ({
      Thread.sleep(100)
      true
    }) {}
    "a"
  }
  val b = IO.interruptible {
    Thread.sleep(100)
    println("b")
    "b"
  }

  IO.race(a, b).map(_.merge).flatMap(IO.println).unsafeRunSync()

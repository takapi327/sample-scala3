package essentialEffect

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration.*
import scala.concurrent.ExecutionContext.Implicits.global

import cats.syntax.all.*

import cats.effect.*
import cats.effect.implicits.*

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

@main def together: Unit =
  def tickingClock: IO[Unit] =
    for
      _ <- IO.println(System.currentTimeMillis)
      _ <- IO.sleep(1.second)
      _ <- tickingClock
    yield ()
  val ohNoes =
    IO.sleep(2.seconds) *> IO.raiseError(new RuntimeException("oh noes!")) // 1
  val together =
    (tickingClock, ohNoes).parTupled

  together.unsafeRunSync()

object Cancel extends IOApp:

  override def run(args: List[String]): IO[ExitCode] =
    for
      fiber <- task.onCancel(IO("i was cancelled").debug.void).start // 1
      _     <- IO("pre-cancel").debug
      _     <- fiber.cancel // 2
      _     <- IO("canceled").debug
    yield ExitCode.Success

  val task: IO[String] =
    IO("task").debug *>
      IO.never // 3

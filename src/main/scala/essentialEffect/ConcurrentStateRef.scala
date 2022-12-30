package essentialEffect

import scala.concurrent.duration.*

import cats.implicits.*

import cats.effect.*

object ConcurrentStateRef extends IOApp:

  override def run(args: List[String]): IO[ExitCode] =
    for
      ticks <- Ref[IO].of(0L) // 1
      _ <- (tickingClock(ticks), printTicks(ticks)).parTupled // 2
    yield ExitCode.Success

  def tickingClock(ticks: Ref[IO, Long]): IO[Unit] =
    for
      _ <- IO.sleep(1.second)
      _ <- IO(System.currentTimeMillis).debug
      _ <- ticks.update(_ + 1) // 3
      _ <- tickingClock(ticks)
    yield ()

  def printTicks(ticks: Ref[IO, Long]): IO[Unit] =
    for
      _ <- IO.sleep(5.seconds)
      n <- ticks.get // 4
      _ <- IO(s"TICKS: $n").debug
      _ <- printTicks(ticks)
    yield ()

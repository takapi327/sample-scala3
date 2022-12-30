package essentialEffect

import scala.concurrent.duration.*

import cats.implicits.*

import cats.effect.*
import cats.effect.std.CountDownLatch

object IsThirteenLatch extends IOApp:

  override def run(args: List[String]): IO[ExitCode] =
    for
      latch <- CountDownLatch[IO](13)
      _ <- (beeper(latch), tickingClock(latch)).parTupled
    yield ExitCode.Success

  def beeper(latch: CountDownLatch[IO]) =
    for
      _ <- latch.await
      _ <- IO("BEEP!").debug
    yield ()

  def tickingClock(latch: CountDownLatch[IO]): IO[Unit] =
    for
      _ <- IO.sleep(1.second)
      _ <- IO(System.currentTimeMillis).debug
      _ <- latch.release
      _ <- tickingClock(latch)
    yield ()

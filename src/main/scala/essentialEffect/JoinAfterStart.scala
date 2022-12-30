package essentialEffect

import scala.concurrent.duration.*

import cats.effect.*

object JoinAfterStart extends IOApp:

  override def run(args: List[String]): IO[ExitCode] =
    for
      fiber <- task.start
      _     <- IO("pre-join").debug
      _     <- fiber.join.debug // 2
      _     <- IO("post-join").debug
    yield ExitCode.Success

  val task: IO[String] =
    IO.sleep(2.seconds) *> IO("task").debug // 1

package essentialEffect

import cats.effect.*

object Start extends IOApp:

  override def run(args: List[String]): IO[ExitCode] =
    for
      _ <- task.start // 1
      _ <- IO("task was started").debug // 2
    yield ExitCode.Success

  val task: IO[String] =
    IO("task").debug

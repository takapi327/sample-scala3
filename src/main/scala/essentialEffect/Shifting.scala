package essentialEffect

import cats.effect.*

object Shifting extends IOApp:

  override def run(args: List[String]): IO[ExitCode] =
    for
      _ <- IO("one").debug
      _ <- IO.cede
      _ <- IO("two").debug
      _ <- IO.cede
      _ <- IO("three").debug
    yield ExitCode.Success

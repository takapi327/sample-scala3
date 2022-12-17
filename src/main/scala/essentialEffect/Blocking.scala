package essentialEffect

import cats.effect.*

object Blocking extends IOApp:

  override def run(args: List[String]): IO[ExitCode] =
    for
      _ <- IO("on default").debug
      _ <- IO.blocking("on blocker").debug // 1
      _ <- IO("where am I?").debug
    yield ExitCode.Success

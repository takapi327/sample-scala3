package essentialEffect

import cats.implicits.*

import cats.effect.*

object BasicResourceComposed extends IOApp:

  override def run(args: List[String]): IO[ExitCode] =
    (stringResource, intResource).tupled // 2
      .use {
        case (s, i) => // 2
          IO(s"$s is so cool!").debug *>
            IO(s"$i is also cool!").debug
      }
      .as(ExitCode.Success)

  val stringResource: Resource[IO, String] =
    Resource.make(
      IO("> acquiring stringResource").debug *> IO("String")
    )(_ => IO("< releasing stringResource").debug.void)

  val intResource: Resource[IO, Int] = // 1
    Resource.make(
      IO("> acquiring intResource").debug *> IO(99)
    )(_ => IO("< releasing intResource").debug.void)

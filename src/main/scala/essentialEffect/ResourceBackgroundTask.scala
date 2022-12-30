package essentialEffect

import scala.concurrent.duration.*

import cats.implicits.*

import cats.effect.*

object ResourceBackgroundTask extends IOApp:

  override def run(args: List[String]): IO[ExitCode] =
    for
      _ <- backgroundTask.use { _ =>
        IO("other work while background task is running").debug *>
          IO.sleep(200.millis) *>
          IO("other work done").debug // 1
      }
      _ <- IO("all done").debug
    yield ExitCode.Success

  val backgroundTask: Resource[IO, Unit] =
    val loop = (IO("looping...").debug *> IO.sleep(100.millis)).foreverM // 2

    Resource.make(
      IO("> forking backgroundTask").debug *> loop.start
    )( // 3
      IO("< canceling backgroundTask").debug.void *> _.cancel // 4
    )
    .void // 5

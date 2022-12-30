package essentialEffect

import java.util.concurrent.Executors

import scala.concurrent.ExecutionContext

import cats.effect.*

object ShiftingMultiple extends IOApp:

  override def run(args: List[String]): IO[ExitCode] =
    for
      _ <- IO("one").debug // 1
      _ <- IO("two").debug.evalOn(ec("1")) // 2
      _ <- IO("three").debug.evalOn(ec("2")) // 3
    yield ExitCode.Success


  def ec(name: String): ExecutionContext = // 5
    ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor { r =>
      val t = new Thread(r, s"pool-$name-thread-1")
      t.setDaemon(true) // 6
      t
    })

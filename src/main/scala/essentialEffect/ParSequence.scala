package essentialEffect

import cats.effect.*
import cats.implicits.*

object ParSequence extends IOApp:

  override def run(args: List[String]): IO[ExitCode] =
    tasks
      .parSequence // 1
      .debug // 2
      .as(ExitCode.Success)

  val numTasks = 100
  val tasks: List[IO[Int]] = List.tabulate(numTasks)(task)

  def task(id: Int): IO[Int] = IO(id).debug // 2

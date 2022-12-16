package essentialEffect

import cats.effect.*
import cats.implicits.*

object ParTraverse extends IOApp:

  override def run(args: List[String]): IO[ExitCode] =
    tasks
      .parTraverse(task) // 1
      .debug // 2
      .as(ExitCode.Success)

  val numTasks = 100
  val tasks: List[Int] = List.range(0, numTasks)

  def task(id: Int): IO[Int] = IO(id).debug
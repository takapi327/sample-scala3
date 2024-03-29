package essentialEffect

import cats.implicits.*

import cats.effect.*

object Parallelism extends IOApp:

  override def run(args: List[String]): IO[ExitCode] =
    for
      _ <- IO(s"number of CPUs: $numCpus").debug
      _ <- tasks.debug
    yield ExitCode.Success

  val numCpus = Runtime.getRuntime().availableProcessors() // 1
  val tasks = List.range(0, numCpus * 2).parTraverse(task) // 2
  def task(i: Int): IO[Int] = IO(i).debug // 3

package essentialEffect

import cats.implicits.*

import cats.effect.*

object RefUpdateImpure extends IOApp:

  override def run(args: List[String]): IO[ExitCode] =
    for
      ref <- Ref[IO].of(0)
      _ <- List(1, 2, 3).parTraverse(task(_, ref)) // 1
    yield ExitCode.Success

  def task(id: Int, ref: Ref[IO, Int]): IO[Unit] =
    ref
      .modify(previous => id -> IO(s"$previous -> $id").debug) // 2
      .flatten
      .replicateA(3) // 3
      .void

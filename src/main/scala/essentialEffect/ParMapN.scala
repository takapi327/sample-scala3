package essentialEffect

import cats.effect.*
import cats.implicits.*

type Test[F[_]] = cats.data.Kleisli[[T] =>> cats.data.OptionT[F, T], org.http4s.Request[F], org.http4s.Response[F]]

extension[A] (ioa: IO[A])
  def debug: IO[A] =
    for
      a <- ioa
    yield
      println(s"[${Thread.currentThread().getName}] $a")
      a

object ParMapN extends IOApp:

  def run(args: List[String]): IO[ExitCode] =
    par.as(ExitCode.Success)

  val hello = IO("hello").debug // 1
  val world = IO("world").debug // 1

  val par = (hello, world)
    .parMapN((h, w) => s"$h $w") // 2
    .debug // 3

object ParMapNErrors extends IOApp:

  def run(args: List[String]): IO[ExitCode] =
    e1.attempt.debug *> // 1
      IO("---").debug *>
      e2.attempt.debug *>
      IO("---").debug *>
      e3.attempt.debug *>
      IO.pure(ExitCode.Success)

  val ok = IO("hi").debug
  val ko1 = IO.raiseError[String](new RuntimeException("oh!")).debug
  val ko2 = IO.raiseError[String](new RuntimeException("noes!")).debug
  val e1 = (ok, ko1).parMapN((_, _) => ())
  val e2 = (ko1, ok).parMapN((_, _) => ())
  val e3 = (ko1, ko2).parMapN((_, _) => ())

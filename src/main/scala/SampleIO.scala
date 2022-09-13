
import java.util.concurrent.Executors

import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.concurrent.duration.*

import cats.effect.*
import cats.effect.unsafe.implicits.global
import cats.implicits.*

/**
 * Fiberにおいて、シングルスレッド/マルチスレッドでの動きをみていく
 *
 * 1. シングルスレッドで、無限ループが発生した場合の処理
 * 2. マルチスレッドで、無限ループが発生した場合の処理
 * 3. シングルスレッド/マルチスレッドで、並列に処理を行うにはどうすればいいのか
 */
object Fiber extends IOApp:
  //val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))
  def run(args: List[String]): IO[ExitCode] = for
    io1 <- loop("A")(0)//.startOn(ec)
    io2 <- loop("B")(0)//.startOn(ec)
    //_   <- io1.join
  //_   <- io2.join
  yield ExitCode.Success

  def loop(id: String)(i: Int): IO[Unit] =
    for
      _      <- IO.println(s"[${Thread.currentThread.getName}] $id")
      _      <- IO.sleep(FiniteDuration.apply(5, SECONDS))
      result <- loop(id)(i + 1)
    yield result

/**
 * Futureにおいて、シングルスレッド/マルチスレッドでの動きをみていく
 *
 * 1. シングルスレッドで、無限ループが発生した場合の処理
 * 2. マルチスレッドで、無限ループが発生した場合の処理
 * 3. シングルスレッド/マルチスレッドで、並列に処理を行うにはどうすればいいのか
 */
@main def FutureSingleThread(): Future[ExitCode] =
  given ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))

  def loop(id: String)(i: Int): Future[Unit] =
    for
      _      <- Future(println(s"[${Thread.currentThread.getName}] $id"))
      _      <- Future(Thread.sleep(200))
      result <- loop(id)(i + 1)
    yield result

  val loop1 = loop("A")(0)
  val loop2 = loop("B")(0)

  val program = for
    //_ <- Future.sequence(List(loop1, loop2))
    //_ <- Future.sequence(List(loop("A")(0), loop("B")(0)))
    _ <- loop("A")(0)
    _ <- loop("B")(0)
  yield ExitCode.Success

  Await.result(program, Duration.Inf)
  program

/**
 * IOを使用する上での問題
 */
object LeakingFibers extends IOApp:
  def run(args: List[String]): IO[ExitCode] = (f1, f2).parMapN {
    case _ => ExitCode.Success
  }

  val f1 = for
    _ <- IO.sleep(FiniteDuration(1000, MILLISECONDS))
    _ <- IO.println("Joined f1")
  yield ()

  val f2 = for
    _ <- IO.raiseError[Unit](new Throwable("boom!"))
    _ <- IO.println("Joined f2")
  yield ()

/**
 * 上記を安全に行う方法
 */
object ResourceIO extends IOApp:
  def run(args: List[String]): IO[ExitCode] =
    (r1.use(_.join), r2.use(_.join)).parMapN {
      case _ => ExitCode.Success
    }

  def safeStart[A](id: String)(io: IO[A]): Resource[IO, Fiber[IO, Throwable, A]] =
    Resource.make(io.start)(fiber => fiber.cancel >> IO.println(s"Joined $id"))

  val r1 = safeStart("1")(IO.sleep(FiniteDuration(1000, MILLISECONDS)))
  val r2 = safeStart("2")(IO.raiseError[Unit](Throwable("boom!")))

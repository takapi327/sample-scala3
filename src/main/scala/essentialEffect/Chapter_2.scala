package essentialEffect

import scala.concurrent.{ Future, ExecutionContext }
//import concurrent.ExecutionContext.Implicits.global

import cats.effect.IO
import cats.effect.unsafe.implicits.global

/**
 * 与えられたFutureを評価し、結果（または失敗）を生成するIOを構築します。
 * Futureは熱心に評価し、またメモを取るので、この関数はパラメータをIOとして受け取り、それは怠惰に評価される可能性があります。
 * この遅延が適切にFutureの定義サイトに戻されると、計算がIOによって完全に管理され、その結果、参照透過的になることが保証されます。
 *
 * 実行スレッドは渡されたものをしようする。
 * Futureの定義サイトに戻される => 遅延評価の実行のこと
 * thunk: => A // AがFuture
 *
 * 計算がIOによって完全に管理され、その結果、参照透過的になることが保証されます
 * 実行して計算を行う処理がIOに委託できるということ
 */
@main def main: Unit =
  /**
   *
   * @return
   * [info] scala-execution-context-global-37
   */
  def futurish1: Future[String] = Future(Thread.currentThread().getName)

  /**
   *
   * @return
   * [info] main
   */
  def futurish2: Future[String] = Future.successful(Thread.currentThread().getName)

  /**
   *
   * @return
   * [info] io-compute-1
   */
  def futurish3: Future[String] = Future(Thread.currentThread().getName)(using cats.effect.unsafe.implicits.global.compute)
  val fut: IO[String] = IO.fromFuture(IO(futurish3))

  (for
    v1 <- fut
    v2 <- fut
  yield
    println(v1)
    println(v2)
    println(Thread.currentThread().getName)).unsafeRunSync()


package library.rdb

import cats.effect.*

import doobie.LogHandler

trait DoobieRepository[M[_]: Async] extends DoobieDatabaseConfig[M]:
  given LogHandler = DoobieLogHandler.trackingLogHandler

import scala.concurrent.ExecutionContext
import com.google.inject.AbstractModule

given ec: ExecutionContext = ExecutionContext.global

case class test() extends DoobieRepository[IO]
case class hoge() extends DoobieRepository[IO]

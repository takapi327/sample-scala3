package library.rdb

import cats.effect.*

import doobie.LogHandler

trait DoobieRepository[M[_]: Async] extends DoobieDatabaseConfig[M], RepositoryClient:
  given LogHandler = DoobieLogHandler.trackingLogHandler

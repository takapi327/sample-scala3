package library.rdb

import doobie.LogHandler

trait DoobieRepository extends DoobieDatabaseConfig:
  given logHandler: LogHandler = DoobieLogHandler.trackingLogHandler

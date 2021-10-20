package library.rdb

import cats.effect.*

import doobie.LogHandler

trait DoobieRepository[M[_]: Async] extends DoobieDatabaseConfig[M]:
  given LogHandler = DoobieLogHandler.trackingLogHandler

import org.atnos.eff._

sealed trait RepositoryOp[A]

trait RepositpryCreator:

  opaque type _repositoryOp[R] = RepositoryOp |= R
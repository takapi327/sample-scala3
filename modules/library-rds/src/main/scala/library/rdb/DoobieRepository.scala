package library.rdb

import cats.effect.*

import doobie.LogHandler

trait DoobieRepository[M[_]: Async] extends DoobieDatabaseConfig[M]:
  given LogHandler = DoobieLogHandler.trackingLogHandler

/*
import doobie.implicits.*
import org.atnos.eff.*
import org.atnos.eff.addon.doobie.DoobieConnectionIOEffect.*
import org.atnos.eff.interpret.*

sealed trait RepositoryOp[A]

final case class UseDoobieRepository[M[_]: Async]() extends RepositoryOp[DoobieRepository[M]]

trait RepositoryCreator:
  type _repositoryOp[R] = RepositoryOp |= R

  def runDoobie[R: _repositoryOp, M[_]: Async](): Eff[R, DoobieRepository[M]] =
    EffCreation.send(UseDoobieRepository())

object RepositoryCreator extends RepositoryCreator

trait RepositoryInterpretation:

  def runRepository[R, U, A](effect: Eff[R, A])(
    using member: Member.Aux[RepositoryOp, R, U],
    connectionIO: _connectionIO[U]
  ): Eff[R, A] =
    translate(effect)(new Translate[RepositoryOp, U] {
      override def apply[X](kv: RepositoryOp[X]): Eff[U, X] =
        kv match
          case UseDoobieRepository () => DoobieRepository[_connectionIO]
    })
*/
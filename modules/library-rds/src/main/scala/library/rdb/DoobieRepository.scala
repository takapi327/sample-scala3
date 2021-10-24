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

/*
import doobie.implicits.*
import org.atnos.eff.*
import org.atnos.eff.addon.doobie.DoobieConnectionIOEffect.*
import org.atnos.eff.interpret.*

import cats.implicits.*
import cats.effect.unsafe.implicits.global

sealed trait RepositoryOp[A]

final case class UseDoobieRepository[T](
  connection: doobie.free.connection.ConnectionIO[T]
) extends RepositoryOp[T]

trait RepositoryCreator:
  //type ThrowableEither[T] = Either[Throwable, T]

  type _repositoryOp[R]    = RepositoryOp |= R
  //type _throwableEither[R] = ThrowableEither |= R

  def runDoobie[R: _repositoryOp, T](
    connection: doobie.free.connection.ConnectionIO[T]
  ): Eff[R, T] =
    EffCreation.send(UseDoobieRepository[T](connection))

object RepositoryCreator extends RepositoryCreator

trait RepositoryInterpretation
  extends DoobieRepository[IO]:

  private def runEffect[R: _connectionIO](connection: doobie.free.connection.ConnectionIO[_]) =
    transactor.use { connection.transact[IO] }.unsafeRunSync()

  def runRepository[R, U, A](effect: Eff[R, A])(
    using member: Member.Aux[RepositoryOp, R, U],
    connectionIO: _connectionIO[U]
  ): Eff[R, A] =
    translate(effect)(new Translate[RepositoryOp, U] {
      override def apply[X](kv: RepositoryOp[X]): Eff[U, X] =
        kv match
          case UseDoobieRepository(connection) => runEffect[U](connection)
    })

object RepositoryInterpretation extends RepositoryInterpretation
*/
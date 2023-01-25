
package ldbc

import cats.{ Monad, Applicative, MonadError, Traverse, Alternative }
import cats.data.Kleisli
import cats.implicits.*

trait ResultSetReader[F[_], T]:

  def read(resultSet: ResultSet[F]): F[T]

object ResultSetReader:
  given [F[_], T](using reader: ResultSetReader[F, Option[T]], error: MonadError[F, Throwable]): ResultSetReader[F, T] with
    override def read(resultSet: ResultSet[F]): F[T] =
      reader.read(resultSet).flatMap {
        case Some(v) => error.pure(v)
        case None    => error.raiseError(new NoSuchElementException(""))
      }
  
  given [F[_]: Monad, T](using table: Kleisli[F, ResultSet[F], T]): ResultSetReader[F, Option[T]] with
    override def read(resultSet: ResultSet[F]): F[Option[T]] =
      for
        hasNext <- resultSet.next()
        result <- if hasNext then table.run(resultSet).map(v => Some(v)) else Monad[F].pure(None)
      yield result
  
  given [F[_]: Monad, T, S[_]: Traverse: Alternative](using table: Kleisli[F, ResultSet[F], T]): ResultSetReader[F, S[T]] with
    override def read(resultSet: ResultSet[F]): F[S[T]] = Monad[F].whileM[S, T](resultSet.next())(table.run(resultSet))

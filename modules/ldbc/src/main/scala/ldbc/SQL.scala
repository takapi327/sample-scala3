
package ldbc

import cats.data.Kleisli
import cats.implicits.*

import cats.effect.*

import example.*

case class SQL[F[_]: Sync](statement: String, params: Seq[ParameterBinder[F]]):

  def query[T](dataSource: javax.sql.DataSource)(func: ResultSet[F] => F[T]): Resource[F, T] =
    for
      connection <- DataSourceResource[F](dataSource).getConnection
      statement  <- connection.prepareStatement(statement)
      result <- Resource.eval(
        params.zipWithIndex.traverse {
          case (param, index) => param.bind(statement, index + 1)
        }
      ) >> Resource.eval(statement.executeQuery())
      v <- Resource.eval(func(result))
    yield v

  def query[T](func: ResultSet[F] => F[T]): Kleisli[F, DataSourceResource[F], T] = Kleisli { dataSource =>
    (for
      connection <- dataSource.getConnection
      statement <- connection.prepareStatement(statement)
    yield statement).use(statement =>
      for
        resultSet <- params.zipWithIndex.traverse {
          case (param, index) => param.bind(statement, index + 1)
        } >> statement.executeQuery()
        result <- func(resultSet)
      yield result
    )
  }

  def query[T](using reader: ResultSetReader[F, T]): Query[F, T] = Query(this, Kleisli { dataSource =>
    (for
      connection <- dataSource.getConnection
      statement <- connection.prepareStatement(statement)
    yield statement).use(statement =>
      for
        resultSet <- params.zipWithIndex.traverse {
          case (param, index) => param.bind(statement, index + 1)
        } >> statement.executeQuery()
        result <- reader.read(resultSet)
      yield result
    )
  })

object SQL:

  trait Parameter[F[_], -T]:
    def bind(statement: PreparedStatement[F], index: Int, value: T): F[Unit]

  object Parameter:

    given [F[_]]: Parameter[F, String] with
      override def bind(statement: PreparedStatement[F], index: Int, value: String): F[Unit] =
        statement.setString(index, value)

    given [F[_]]: Parameter[F, Int] with
      override def bind(statement: PreparedStatement[F], index: Int, value: Int): F[Unit] =
        statement.setInt(index, value)

    given [F[_]]: Parameter[F, Double] with
      override def bind(statement: PreparedStatement[F], index: Int, value: Double): F[Unit] =
        statement.setDouble(index, value)

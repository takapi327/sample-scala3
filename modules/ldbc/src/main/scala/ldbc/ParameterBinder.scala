
package ldbc

trait ParameterBinder[F[_]]:
  def bind(statement: PreparedStatement[F], index: Int): F[Unit]

object ParameterBinder:
  def apply[F[_], T](value: T)(using param: SQL.Parameter[F, T]) =
    new ParameterBinder[F]:
      override def bind(statement: PreparedStatement[F], index: Int): F[Unit] =
        param.bind(statement, index, value)

  given [F[_], T](using SQL.Parameter[F, T]): Conversion[T, ParameterBinder[F]] with
    override def apply(x: T): ParameterBinder[F] = ParameterBinder[F, T](x)

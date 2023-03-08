
package ldbc

trait Query[F[_]]:

  def statement: String

  def params: Seq[ParameterBinder[F]]

case class Select[F[_]](
  statement: String,
  params:    Seq[ParameterBinder[F]]
) extends Query[F]:

  def where(sql: SQL[F]): Where[F] =
    Where[F](statement + " WHERE " + sql.statement, params ++ sql.params)

case class Where[F[_]](
  statement: String,
  params:    Seq[ParameterBinder[F]]
) extends Query[F]:

  def and(sql: SQL[F]): Where[F] =
    this.copy(statement + " AND " + sql.statement, params ++ sql.params)

  def or(sql: SQL[F]): Where[F] =
    this.copy(statement + " OR " + sql.statement, params ++ sql.params)

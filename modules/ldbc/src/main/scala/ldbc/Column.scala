
package ldbc

trait Column[F[_]]:

  type ThisType

  def label: String

  def types: Seq[Column.Type]

  def loader: ResultSetLoader[F, ThisType]

  final def get(resultSet: ResultSet[F]): F[ThisType] =
    resultSet.get[ThisType](label)(using loader)

object Column:

  enum Type:
    case NOT_NULL


package ldbc

import cats.Applicative
import cats.data.Kleisli
import cats.implicits.*

trait Table[F[_], T <: Product](name: String) extends example.ColumnAlias:

  type Schema = Kleisli[F, ResultSet[F], T]

  def * : Seq[Column[F]]

  protected final def column[A](
    _label: String,
    _type: Column.Type,
    _options: ColumnOption.Key*
  )(using _loader: ResultSetLoader[F, A]): Column[F] = new Column[F]:
    override type ThisType = A

    override def label: String = _label

    override def `type`: Column.Type = _type

    override def options: Seq[ColumnOption.Key] = _options

    override def loader: ResultSetLoader[F, A] = _loader

  def options: Seq[ColumnOption] = Seq.empty

  given schema: Schema

  def createTableQuery: String =
    val primaryKey: Option[String] =
      val columns = this.*.filter(_.options.contains(PrimaryKey))
      if columns.nonEmpty then
        Some(ColumnOption.PrimaryKey(columns.map(_.label): _*).toString)
      else None

    val settings: Seq[String] =
      *.map(_.toString) ++ options.map(_.toString)

    s"""
       |CREATE TABLE `$name` (
       |  ${settings.mkString(",\n  ")}
       |);
       |
       |""".stripMargin

object Table:

  object Schema:
    def apply[F[_]: Applicative, T <: Product](table: Table[F, T])(
      using mirror: scala.deriving.Mirror.ProductOf[T]
    ): Kleisli[F, ResultSet[F], T] = Kleisli { resultSet =>
      table.*.traverse(_.get(resultSet).asInstanceOf[F[Any]]).map(v =>
        mirror.fromProduct(Tuple.fromArray(v.toArray))
      )
    }
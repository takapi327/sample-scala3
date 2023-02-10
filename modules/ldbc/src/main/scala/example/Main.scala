
package example

import cats.*
import cats.implicits.*
import cats.data.Kleisli
import cats.effect.*
import ldbc.*
import SQL.*
import ldbc.generic.{Schema, SchemaType}
import ldbc.generic.semiauto.*
import ldbc.ResultSetReader.given

object Main extends IOApp.Simple, MySQLConfigReader, SQLSyntax[IO]:

  val jpn = "JPN"
  val name = "Tokyo"

  override def run: IO[Unit] =
    case class Country(code: String, name: String, population: Int, gnp: Option[Double])

    object Country extends Table[IO, Country]("country"):

      val code: Column[IO] = column[String]("code", CHAR(3))
      val name: Column[IO] = column[String]("name", TEXT())
      val population: Column[IO] = column[Int]("population", INT(11))
      val gnp: Column[IO] = column[Option[Double]]("gnp", DECIMAL(10, 2))

      def * = Seq(code, name, population, gnp)

      given schema: Schema = Table.Schema(this)

    sql"SELECT * FROM country WHERE code = $jpn AND name = $name"
      .query[List[Country]].connect.run(DataSourceResource(buildDataSource)).map(println(_))

case class Query[F[_], T](
  private val sql: SQL[F],
  connect: Kleisli[F, DataSourceResource[F], T]
)

trait SQLSyntax[F[_]: Sync]:
  extension (sc: StringContext)
    inline def sql(inline args: ParameterBinder[F]*): SQL[F] =
      val strings = sc.parts.iterator
      val expressions = args.iterator
      SQL(strings.mkString("?"), expressions.toSeq)

  export SQL.Parameter.given

import Column.*
import scala.compiletime.*
import ColumnOption.Key

trait ColumnAlias:

  protected val AutoInc: Key = Key.AutoInc
  protected val PrimaryKey: Key = Key.PrimaryKey
  protected val Unique: Key = Key.Unique

  inline def CHAR(inline length: Int, inline character: Option[Character] = None): Char =
    inline if length < 0 || length > 255 then
      error("The length of the CHAR must be in the range 0 to 255.")
    else Char(length, character)

  inline def VARCHAR(inline length: Int, inline character: Option[Character] = None): Varchar =
    inline if length < 0 || length > 255 then
      error("The length of the VARCHAR must be in the range 0 to 255.")
    else Varchar(length, character)

  inline def BINARY(inline length: Int, inline character: Option[Character] = None): Binary =
    inline if length < 0 || length > 255 then
    error("The length of the BINARY must be in the range 0 to 255.")
    else Binary(length, character)

  inline def VARBINARY(inline length: Int, inline character: Option[Character] = None): Varbinary =
    inline if length < 0 || length > 255 then
      error("The length of the VARBINARY must be in the range 0 to 255.")
    else Varbinary(length, character)

  inline def TINYBLOB(inline character: Option[Character] = None): Tinyblob =
    Tinyblob(character)

  inline def BLOB(inline length: Long, inline character: Option[Character] = None): Blob =
    inline if length < 0 || length > 4294967295L then
      error("The length of the BLOB must be in the range 0 to 4294967295.")
    else Blob(length, character)

  inline def MEDIUMBLOB(inline character: Option[Character] = None): Mediumblob =
    Mediumblob(character)

  inline def TINYTEXT(inline character: Option[Character] = None): Tinytext =
    Tinytext(character)

  inline def TEXT(inline character: Option[Character] = None): Text =
    Text(character)

  inline def MEDIUMTEXT(inline character: Option[Character] = None): Mediumtext =
    Mediumtext(character)

  inline def LONGTEXT(inline character: Option[Character] = None): Longtext =
    Longtext(character)

  //inline def ENUM(inline values: String): Enum =
  //  inline if values.isEmpty then
  //    error(s"The maximum number of values that can be set for ENUM is 65,535.")
  //  else Enum(values.split(",").toList)
//
  //inline def SET(inline values: List[String], inline character: Option[Character]): CSet =
  //  if values.length > 64 then
  //    error("The maximum number of values that can be set for SET is 64.")
  //  else CSet(values, character)

  inline def TINYINT(inline length: Int): Tinyint =
    inline if length < 0 || length > 255 then
      error("The length of the TINYINT must be in the range 0 to 255.")
    else Tinyint(length)

  inline def SMALLINT(inline length: Int): Smallint =
    inline if length < 0 || length > 255 then
      error("The length of the SMALLINT must be in the range 0 to 255.")
    else Smallint(length)

  inline def MEDIUMINT(inline length: Int): Mediumint =
    inline if length < 0 || length > 255 then
      error("The length of the MEDIUMINT must be in the range 0 to 255.")
    else Mediumint(length)

  inline def INT(inline length: Int): CInt =
    inline if length < 0 || length > 255 then
      error("The length of the INT must be in the range 0 to 255.")
    else CInt(length)

  inline def BIGINT(inline length: Int): Bigint =
    inline if length < 0 || length > 255 then
    error("The length of the BIGINT must be in the range 0 to 255.")
    else Bigint(length)

  inline def DECIMAL(inline accuracy: Int = 10, inline scale: Int = 0): Decimal =
    inline if accuracy < 0 then error("The value of accuracy for DECIMAL must be an integer.")
    inline if scale < 0 then error("The DECIMAL scale value must be an integer.")
    inline if accuracy > 65 then
      error("The maximum number of digits for DECIMAL is 65.")
    else Decimal(accuracy, scale)

  inline def FLOAT(inline accuracy: Int): Float =
    inline if accuracy < 0 || accuracy > 53 then
      error("The length of the FLOAT must be in the range 0 to 53.")
    else Float(accuracy)

  inline def BIT(inline length: Int): Bit =
    inline if length < 1 || length > 64 then
      error("The length of the BIGINT must be in the range 1 to 64.")
    else Bit(length)

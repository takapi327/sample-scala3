
package ldbc

import org.specs2.mutable.Specification

import cats.effect.IO

import Column.*

import example.Main.*
import cats.effect.unsafe.implicits.global
import cats.Applicative
import cats.data.Kleisli
import cats.implicits.*

import ldbc.implicits.{ *, given }

def PRIMARY_KEY[F[_]]: PrimaryKey[F] = PrimaryKey[F]()

object ColumnTest extends Specification, example.SQLSyntax[IO]:

  "" should {
    "" in {
      VARCHAR[String](255).columQuery === "VARCHAR(255) NOT NULL"
    }

    "" in {

      val table: TableTest[IO, Country] = Table1[IO, Country]("country_test")(
        column("id",         TINYINT(255).AUTO_INCREMENT(AutoInc.Key.UNIQUE_KEY), "識別子"),
        column("code",       CHAR(3)),
        column("name",       TEXT()),
        column("population", INT(11)),
        column("gnp",        DOUBLE(25)),
        column("updated_at", TIMESTAMP.DEFAULT_CURRENT_TIMESTAMP()),
        column("created_at", TIMESTAMP.DEFAULT_CURRENT_TIMESTAMP(true)),
      )
        .setPrimaryKey("id", "code")
        .setUniqueKey("id", "code")

      println("===============")
      println(table.createTableQuery)
      //given Kleisli[IO, ResultSet[IO], Country] = TableTest.Schema(test)
      //sql"SELECT code, name, population, gnp, updated_at, created_at FROM country_test WHERE code = $jpn AND name = $name"
      //  .query[List[Country]].connect.run(DataSourceResource(buildDataSource)).map(println(_)).unsafeRunSync()
      //test.select(colum => colum.*)
      //  .query[List[Country]].connect.run(DataSourceResource(buildDataSource)).map(println(_)).unsafeRunSync()
      println("===============")

      val name = "Tokyo"
      table.select[(String, String, Int)](v => (v.code, v.name, v.population))
        .where[Option](sql"name = $name")
        .run(DataSourceResource(buildDataSource))
        .map(println(_))
        .unsafeRunSync()
      println("=====================")
      table.selectAll
        .where[List](sql"name = $name")
        .run(DataSourceResource(buildDataSource))
        .map(println(_))
        .unsafeRunSync()
      println("=====================")
      //table.select[(String, String)](v => (v.code, v.name))
      //  .unsafeWhere(sql"name = $name")
      //  .run(DataSourceResource(buildDataSource))
      //  .map(println(_))
      //  .unsafeRunSync()
      println("=====================")
      true
    }
  }

object Schema:
  def apply[F[_]: Applicative, T <: Product](table: TableTest[F, T])(
    using mirror: scala.deriving.Mirror.ProductOf[T]
  ): Kleisli[F, ResultSet[F], T] = Kleisli { resultSet =>
    table.*.traverse(_.get(resultSet)).map(v =>
      mirror.fromProduct(Tuple.fromArray(v.toArray))
    )
  }

case class Country(
                    id:   Byte,
                    code: String,
                    name: String,
                    population: Int,
                    gnp: Option[Double],
                    updatedAt: java.time.LocalDateTime,
                    createdAt: java.time.LocalDateTime,
                  )

//object Country extends Table[IO, Country]("country"):
//
//  val code: Column[IO] = column[String]("code", CHAR(3), PrimaryKey, AutoInc)
//  val name: Column[IO] = column[String]("name", TEXT())
//  val population: Column[IO] = column[Int]("population", INT(11))
//  val gnp: Column[IO] = column[Option[Double]]("gnp", DECIMAL(10, 2))
//
//  def * = Seq(code, name, population, gnp)
//
//  override def options: Seq[ColumnOption] = Seq(
//    ColumnOption.PrimaryKey(code.label),
//    ColumnOption.Unique(population.label, gnp.label)
//  )
//
//  given schema: Schema = Table.Schema(this)

trait ColumnTest[F[_], T]:
  self =>

  def label: String

  def dataType: DataType[T]

  def comment: Option[String]

  def loader: ResultSetLoader[F, T]

  final def get(resultSet: ResultSet[F]): F[T] =
    resultSet.get[T](label)(using loader)

  def columnQuery: String =
    s"`$label` $dataType" + comment.fold("")(str => s" COMMENT '$str'")

  override def toString: String = s"`$label`"

  private def copy(
    _label:    String = self.label,
    _dataType: DataType[T] = self.dataType,
    _comment:  Option[String] = self.comment
  ): ColumnTest[F, T] = new ColumnTest[F, T]:
    override def label: String = _label
    override def dataType: DataType[T] = _dataType
    override def comment: Option[String] = _comment
    override def loader: ResultSetLoader[F, T] = self.loader

  def updateDataType(_dataType: DataType[T]): ColumnTest[F, T] =
    this.copy(_dataType = _dataType)

type DataTypeTuples[Types <: Tuple, F[_]] = Types match
  case t *: EmptyTuple => ColumnTest[F, t]
  case _ => Tuple.Map[Types, [t] =>> ColumnTest[F, t]]

opaque type DataTypeElemsNormalizer[Types <: Tuple, F[_]] = DataTypeTuples[Types, F] => Tuple.Map[Types, [T] =>> ColumnTest[F, T]]

object DataTypeElemsNormalizer:
  given [T, F[_]]: DataTypeElemsNormalizer[T *: EmptyTuple, F] = (input: ColumnTest[F, T]) => input *: EmptyTuple
  given [T1, T2, F[_]]: DataTypeElemsNormalizer[(T1, T2), F] = identity
  given [T1, T2, TS <: NonEmptyTuple, F[_]](using normalizer: DataTypeElemsNormalizer[T2 *: TS, F]): DataTypeElemsNormalizer[T1 *: T2 *: TS, F] =
    (elems: ColumnTest[F, T1] *: DataTypeTuples[T2 *: TS, F]) => elems.head *: normalizer(elems.tail).asInstanceOf[ColumnTest[F, T2] *: Tuple.Map[TS, [T] =>> ldbc.ColumnTest[F, T]]]

  def normalize[Types <: Tuple, F[_]](in: DataTypeTuples[Types, F])(using normalizer: DataTypeElemsNormalizer[Types, F]): Tuple.Map[Types, [T] =>> ColumnTest[F, T]] =
    normalizer(in)

def column[F[_], A](
  _label: String,
  _dataType: DataType[A],
)(using _loader: ResultSetLoader[F, A]): ColumnTest[F, A] = new ColumnTest[F, A]:
  override def label: String = _label
  override def dataType: DataType[A] = _dataType
  override def comment: Option[String] = None
  override def loader: ResultSetLoader[F, A] = _loader

def column[F[_], A](
  _label: String,
  _dataType: DataType[A],
  _comment:  String
)(using _loader: ResultSetLoader[F, A]): ColumnTest[F, A] = new ColumnTest[F, A]:
  override def label: String = _label
  override def dataType: DataType[A] = _dataType
  override def comment: Option[String] = Some(_comment)
  override def loader: ResultSetLoader[F, A] = _loader

object T extends example.ColumnAlias
import T.*
import scala.compiletime.ops.int.S

object Tuples:
  type IndexOf[T <: Tuple, E] <: Int = T match
    case E *: _  => 0
    case _ *: es => S[IndexOf[es, E]]

type ColumnType[F[_], X] = X match
  case ColumnTest[F, x] => x

type InverseMap[F[_], X <: Tuple] <: Tuple = X match {
  case x *: t => ColumnTest[F, x] *: InverseMap[F, t]
  case EmptyTuple => EmptyTuple
}

import scala.language.dynamics
import cats.InvariantMonoidal
trait TableTest[F[_], P <: Product] extends Dynamic:

  def selectDynamic[Tag <: Singleton](tag: Tag)(
    using mirror: scala.deriving.Mirror.ProductOf[P], index: ValueOf[Tuples.IndexOf[mirror.MirroredElemLabels, Tag]]
  ): ColumnTest[F, Tuple.Elem[mirror.MirroredElemTypes, Tuples.IndexOf[mirror.MirroredElemLabels, Tag]]]

  def * : List[ColumnTest[F, Any]]

  def primaryKey: Option[PrimaryKeyTest[F]] = None
  def uniqueKey: Option[UniqueKeyTest[F]] = None

  def setPrimaryKey(keys: String*): TableTest[F, P]
  def setUniqueKey(keys: String*): TableTest[F, P]

  def createTableQuery: String

  def select[A <: Tuple](func: TableTest[F, P] => Tuple.Map[A, [t] =>> ColumnTest[F, t]]): Query[F, A]
  def selectAll(using mirror: scala.deriving.Mirror.ProductOf[P]): Query[F, P]

object TableTest:

  object Schema:
    def apply[F[_]: Applicative, T <: Product](
      table: TableTest[F, T]
    ): scala.deriving.Mirror.ProductOf[T] ?=> Kleisli[F, ResultSet[F], T] = Kleisli { resultSet =>
      val mirror = summon[scala.deriving.Mirror.ProductOf[T]]
      table.*.traverse(_.get(resultSet).asInstanceOf[F[Any]]).map(v =>
        mirror.fromProduct(Tuple.fromArray(v.toArray))
      )
    }

import ResultSetReader.given
case class Query[F[_]: Applicative: cats.effect.Sync: cats.Monad, T](
  statement:  String,
  readColumn: Kleisli[F, ResultSet[F], T]
):

  given Kleisli[F, ResultSet[F], T] = readColumn

  def where[S[_]: cats.Traverse: cats.Alternative](sql: SQL[F]): Kleisli[F, DataSourceResource[F], S[T]] = Kleisli { dataSource =>
    (for
      connection <- dataSource.getConnection
      statement  <- connection.prepareStatement(statement + " WHERE " + sql.statement)
    yield statement).use(statement =>
      for
        resultSet <- sql.params.zipWithIndex.traverse {
          case (param, index) => param.bind(statement, index + 1)
        } >> statement.executeQuery()
        result <- summon[ResultSetReader[F, S[T]]].read(resultSet)
      yield result
    )
  }

  def unsafeWhere(sql: SQL[F]): Kleisli[F, DataSourceResource[F], T] = Kleisli { dataSource =>
    (for
      connection <- dataSource.getConnection
      statement <- connection.prepareStatement(statement + " WHERE " + sql.statement)
    yield statement).use(statement =>
      for
        resultSet <- sql.params.zipWithIndex.traverse {
          case (param, index) => param.bind(statement, index + 1)
        } >> statement.executeQuery()
        result <- summon[ResultSetReader[F, T]].read(resultSet)
      yield result
    )
  }

case class PrimaryKeyTest[F[_]](column: ColumnTest[F, ?]*):

  def queryString: String = if column.nonEmpty then s"PRIMARY KEY(${column.mkString(", ")})" else "PRIMARY KEY"

  override def toString: String = "PRIMARY KEY"

case class UniqueKeyTest[F[_]](column: ColumnTest[F, ?]*):

  def queryString: String = if column.nonEmpty then s"UNIQUE KEY(${column.mkString(", ")})" else "UNIQUE KEY"

  override def toString: String = "UNIQUE KEY"

class TableTestImpl[F[_]: cats.effect.Sync: cats.Monad, P <: Product, T <: Tuple](
  name: String,
  tuple: Tuple.Map[T, [t] =>> ColumnTest[F, t]],
  primaryKey: Option[PrimaryKeyTest[F]] = None,
  uniqueKey: Option[UniqueKeyTest[F]] = None
) extends TableTest[F, P]:

  def selectDynamic[Tag <: Singleton](tag: Tag)(
    using mirror: scala.deriving.Mirror.ProductOf[P], index: ValueOf[Tuples.IndexOf[mirror.MirroredElemLabels, Tag]]
  ): ColumnTest[F, Tuple.Elem[mirror.MirroredElemTypes, Tuples.IndexOf[mirror.MirroredElemLabels, Tag]]] =
    tuple.productElement(index.value).asInstanceOf[ColumnTest[F, Tuple.Elem[mirror.MirroredElemTypes, Tuples.IndexOf[mirror.MirroredElemLabels, Tag]]]]

  def * : List[ColumnTest[F, Any]] = tuple.toList.asInstanceOf[List[ColumnTest[F, Any]]]

  def setPrimaryKey(keys: String*): TableTest[F, P] =
    val filter = *.filter(v => keys.contains(v.label))
    new TableTestImpl[F, P, T](
      name       = this.name,
      tuple      = this.tuple,
      primaryKey = if filter.nonEmpty then Some(PrimaryKeyTest(filter: _*)) else None,
      uniqueKey  = this.uniqueKey
    )

  def setUniqueKey(keys: String*): TableTest[F, P] =
    val filter = *.filter(v => keys.contains(v.label))
    new TableTestImpl[F, P, T](
      name       = this.name,
      tuple      = this.tuple,
      primaryKey = this.primaryKey,
      uniqueKey  = if filter.nonEmpty then Some(UniqueKeyTest(filter: _*)) else None
    )

  def readColumn[A](columns: List[ColumnTest[F, Any]]): Kleisli[F, ResultSet[F], A] = Kleisli { resultSet =>
    columns.traverse(_.get(resultSet).asInstanceOf[F[Any]]).map(v =>
      Tuple.fromArray(v.toArray).asInstanceOf[A]
    )
  }

  def select[A <: Tuple](func: TableTest[F, P] => Tuple.Map[A, [t] =>> ColumnTest[F, t]]): Query[F, A] =
    Query[F, A](
      s"SELECT ${func(this).toList.mkString(",")} FROM $name",
      readColumn[A](func(this).toList.asInstanceOf[List[ColumnTest[F, Any]]])
    )

  def selectAll(using mirror: scala.deriving.Mirror.ProductOf[P]): Query[F, P] =
    Query[F, P](
      s"SELECT ${*.mkString(",")} FROM $name",
      readColumn[mirror.MirroredElemTypes](*).map(mirror.fromProduct(_))
    )

  val columnDefinitions: Seq[String] =
    *.map(column => {
      primaryKey.fold(column)(key => {
        if key.column.contains(column) then
          column.dataType match
            case d: DataType.Tinyint[?] =>
              if d.autoInc.nonEmpty && d.autoInc.get.key.contains(AutoInc.Key.PRIMARY_KEY) then column.updateDataType(d.updateAutoInc(Some(AutoInc(None))))
              else column
            case _: DataType[?] => column
        else column
      })
    }.columnQuery)

  private val options: Seq[String] =
    columnDefinitions ++ Seq(primaryKey.map(_.queryString), uniqueKey.map(_.queryString)).flatten

  def createTableQuery: String =
    s"""
       |CREATE TABLE `$name` (
       |  ${options.mkString(",\n  ")}
       |);
       |
       |""".stripMargin

object Table1 extends Dynamic:

  def applyDynamic[F[_]: cats.effect.Sync, T <: Product](nameApply: "apply")(
    using mirror: scala.deriving.Mirror.ProductOf[T], normalizer: DataTypeElemsNormalizer[mirror.MirroredElemTypes, F]
  )(name: String)(elems: DataTypeTuples[mirror.MirroredElemTypes, F]): TableTest[F, T] =
    fromTupleMap[F, T](name, DataTypeElemsNormalizer.normalize(elems))

  private def fromTupleMap[F[_]: cats.effect.Sync, T <: Product](using mirror: scala.deriving.Mirror.ProductOf[T])(
    name:     String,
    tupleMap: Tuple.Map[mirror.MirroredElemTypes, [t] =>> ColumnTest[F, t]]
  ): TableTest[F, T] = new TableTestImpl(name, tupleMap)

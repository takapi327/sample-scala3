
import java.time.LocalDateTime

import com.mysql.cj.jdbc.MysqlDataSource

import cats.data.Kleisli
import cats.implicits.*

import cats.effect.*

import ldbc.sql.*
import ldbc.sql.syntax.{ *, given }
import ldbc.dsl.io.*

case class User(
  id:        Long,
  name:      String,
  age:       Option[Int],
  status:    User.Status,
  updatedAt: LocalDateTime,
  createdAt: LocalDateTime
)

object User:

  enum Status(val code: Short):
    case Active extends Status(1)

object Example extends App:
  private val dataSource = new MysqlDataSource()
  dataSource.setServerName("127.0.0.1")
  dataSource.setPortNumber(13306)
  //dataSource.setDatabaseName("ldbc_example")
  dataSource.setUser("takapi327")
  dataSource.setPassword("takapi327")

  val conn = dataSource.getConnection
  val meta = conn.getMetaData

  //println(meta.getDatabaseProductName)
  //println(meta.getDatabaseProductVersion)
  //println(meta.getSQLKeywords)
  //println(meta.getSystemFunctions)
  //println(meta.getStringFunctions)
  //println(meta.getNumericFunctions)
  //println(meta.getTimeDateFunctions)
  //println(meta.getIdentifierQuoteString)

  val resultSet = meta.getColumns(null, null, "user", "%")

  while (resultSet.next()) {
    //println(resultSet.getString("COLUMN_NAME") + ": " + resultSet.getString("TYPE_NAME") + "=" + resultSet.getInt("COLUMN_SIZE"))
    println(resultSet.getString("COLUMN_NAME") + ": " + resultSet.getString("COLUMN_DEF"))
  }

  resultSet.close()
  conn.close()


object Main extends IOApp:

  private val dataSource = new MysqlDataSource()
  dataSource.setServerName("127.0.0.1")
  dataSource.setPortNumber(13306)
  dataSource.setDatabaseName("sample_doobie")
  dataSource.setUser("takapi327")
  dataSource.setPassword("takapi327")

  given Conversion[SMALLINT[Short], DataType[User.Status]] = DataType.mapping[SMALLINT[Short], User.Status]

  private val table: Table[User] = Table[User]("user")(
    column("id", BIGINT(64), AUTO_INCREMENT, PRIMARY_KEY),
    column("name", VARCHAR(255)),
    column("age", INT(255).DEFAULT_NULL),
    column("status", SMALLINT(255)),
    column("updated_at", TIMESTAMP.DEFAULT_CURRENT_TIMESTAMP()),
    column("created_at", TIMESTAMP.DEFAULT_CURRENT_TIMESTAMP(true))
  )

  given ResultSetReader[IO, User.Status] =
    ResultSetReader.mapping[IO, Short, User.Status](v => User.Status.values.find(_.code === v).get)

  given Kleisli[IO, ResultSet[IO], User] =
    for
      id        <- table.id()
      name      <- table.name()
      age       <- table.age()
      status    <- table.status()
      updatedAt <- table.updatedAt()
      createdAt <- table.createdAt()
    yield User(id, name, age, status, updatedAt, createdAt)

  override def run(args: List[String]): IO[ExitCode] =
    (for
      //updated <- sql"INSERT INTO user (id, name, age) VALUES ($None, 'test', $None)".update()
      user <- sql"SELECT * FROM user WHERE id = ${ 1 }".query
    yield
      //println(updated)
      println(user)
    )
      .transaction
      .run(dataSource)
      .as(ExitCode.Success)

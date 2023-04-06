
import java.time.LocalDateTime
import com.mysql.cj.jdbc.MysqlDataSource

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

import slick.jdbc.GetResult
import slick.jdbc.MySQLProfile.api.*
import slick.lifted.ShapedValue
import slick.sql.SqlProfile.ColumnOption.SqlType

case class User(
  id:   Option[Long],
  name: String,
  age:  Option[Int],
)

object Example:

  private val dataSource = new MysqlDataSource()
  dataSource.setServerName("127.0.0.1")
  dataSource.setPortNumber(13306)
  dataSource.setDatabaseName("sample_doobie")
  dataSource.setUser("takapi327")
  dataSource.setPassword("takapi327")

  class UserTable(tag: Tag) extends Table[User](tag, "user"):
    def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def age = column[Option[Int]]("age")

    def * = (id, name, age).mapTo[User]

  private val userTable = TableQuery(new UserTable(_))
  private val db = Database.forDataSource(dataSource, None)

  def main(args: Array[String]): Unit =

    val result = db.run(userTable.filter(_.name === "takapi").result.headOption)
    result.map(println(_))

    Await.result(result, Duration.Inf)
    db.close


//given GetResult[User] = GetResult(r => Person(r.<<?, r.<<, r.<<?))
//val query = sql"SELECT * FROM user WHERE name = 'takapi'".as[User].headOption

/*
package ldbc.slick

import com.mysql.cj.jdbc.MysqlDataSource

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

import ldbc.core.syntax.given
import ldbc.slick.jdbc.MySQLProfile.api.*


object Example:

  case class User(
    id:   Option[Long],
    name: String,
    age:  Option[Int],
  )

  private val dataSource = new MysqlDataSource()
  dataSource.setServerName("127.0.0.1")
  dataSource.setPortNumber(13306)
  dataSource.setDatabaseName("sample_doobie")
  dataSource.setUser("takapi327")
  dataSource.setPassword("takapi327")

  private val table = Table[User]("user")(
    column("id", BIGINT(64), PRIMARY_KEY, AUTO_INCREMENT),
    column("name", VARCHAR(255)),
    column("age", INT(255).DEFAULT_NULL),
  )

  private val userTable = SlickTableQuery(table)
  private val db = Database.forDataSource(dataSource, None)

  def main(args: Array[String]): Unit =

    val result = db.run(userTable.filter(_.name === "takapi").result.headOption)
    result.map(println(_))

    Await.result(result, Duration.Inf)
    db.close


*/
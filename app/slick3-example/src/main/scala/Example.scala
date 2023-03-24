
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

class UserTable(tag: Tag) extends Table[User](tag, "user"):
  def id   = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name", SqlType("BIGINT(64)"))
  def age  = column[Option[Int]]("age")

  def pk = primaryKey("pk_a", id)

  def * = (id.?, name, age).mapTo[User]

  println("======================")
  println("======================")
  println("======================")
  val test: ShapedValue[Rep[String], String] = name
  val test2: ShapedValue[(Rep[Option[Long]], Rep[String], Rep[Option[Int]]), (Option[Long], String, Option[Int])] =
    (id.?, name, age)
  println(test2)
  println("======================")
  println("======================")
  println("======================")


object Example:

  private val dataSource = new MysqlDataSource()
  dataSource.setServerName("127.0.0.1")
  dataSource.setPortNumber(13306)
  dataSource.setDatabaseName("sample_doobie")
  dataSource.setUser("takapi327")
  dataSource.setPassword("takapi327")

  private val userTable = TableQuery[UserTable]
  private val db = Database.forDataSource(dataSource, None)

  def main(args: Array[String]): Unit =
    //given GetResult[User] = GetResult(r => Person(r.<<?, r.<<, r.<<?))
    //val query = sql"SELECT * FROM user WHERE name = 'takapi'".as[User].headOption
    val result = db.run(userTable.filter(_.name === "takapi").result.headOption)
    result.map(println(_))
    println(userTable.schema.create.statements)

    Await.result(result, Duration.Inf)
    db.close

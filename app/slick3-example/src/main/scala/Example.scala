
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import com.mysql.cj.jdbc.MysqlDataSource
import slick.jdbc.GetResult
import slick.jdbc.MySQLProfile.api.*

case class Person(
  id:   Option[Long],
  name: String,
  age:  Option[Int],
)

class PersonTable(tag: Tag) extends Table[Person](tag, "person"):
  def id   = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")
  def age  = column[Option[Int]]("age")

  def * = (id.?, name, age).mapTo[Person]

object Example:

  private val dataSource = new MysqlDataSource()
  dataSource.setServerName("127.0.0.1")
  dataSource.setPortNumber(13306)
  dataSource.setDatabaseName("sample_doobie")
  dataSource.setUser("takapi327")
  dataSource.setPassword("takapi327")

  private val personTable = TableQuery[PersonTable]
  private val db = Database.forDataSource(dataSource, None)

  def main(args: Array[String]): Unit =
    //val query = for {
    //  p <- personTable.filter(_.name === "takapi327")
    //} yield p.name
    given GetResult[Person] = GetResult(r => Person(r.<<?, r.<<, r.<<?))
    val query = sql"SELECT * FROM person WHERE name = 'takapi327'".as[Person].headOption
    val result = db.run(query)
    result.map(println(_))
    println(personTable.schema.create.statements)

    Await.result(result, Duration.Inf)
    db.close

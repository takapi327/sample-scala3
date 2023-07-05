
import java.time.LocalDateTime

import com.mysql.cj.jdbc.MysqlDataSource

import cats.data.Kleisli
import cats.implicits.*

import cats.effect.*

import ldbc.core.*
import ldbc.sql.*
import ldbc.sql.syntax.{ *, given }
import ldbc.dsl.io.*
import ldbc.schemaspy.SchemaSpyGenerator

import ldbc.generated.sample_ldbc.*

object Main extends IOApp:

  private val dataSource = new MysqlDataSource()
  dataSource.setServerName("127.0.0.1")
  dataSource.setPortNumber(13306)
  dataSource.setDatabaseName("sample_doobie")
  dataSource.setUser("takapi327")
  dataSource.setPassword("takapi327")

  given Kleisli[IO, ResultSet[IO], User] =
    for
      id        <- User.table.id()
      name      <- User.table.name()
      age       <- User.table.age()
      status    <- User.table.status()
      updatedAt <- User.table.updatedAt()
      createdAt <- User.table.createdAt()
    yield User(id, name, age, status, updatedAt, createdAt)

  val db = new Database:

    override val databaseType: Database.Type = Database.Type.MySQL

    override val name: String = "ldbc_example"

    override val schema: String = "ldbc_example"

    override val schemaMeta: Option[String] = None

    override val catalog: Option[String] = Some("def")

    override val characterSet: Option[Character] = None

    override val host: String = "127.0.0.1"

    override val port: Int = 13306

    override val tables = Set(
      AutoIncTest.table,
      Country.table,
      CountryTest.table,
      MysqlColumnTest.table,
      Person.table,
      User.table,
      SubTest.table,
      Test.table,
    )

  val file = java.io.File("document")

  override def run(args: List[String]): IO[ExitCode] =
    (for
    //updated <- sql"INSERT INTO user (id, name, age) VALUES ($None, 'test', $None)".update()
      users <- sql"SELECT * FROM user".query[List[User]]
    yield
      //println(updated)
      println(users)
      //SchemaSpyGenerator.default(db, file).generate()
    )
      .transaction
      .run(dataSource)
      .as(ExitCode.Success)

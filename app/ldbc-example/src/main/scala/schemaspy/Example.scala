package schemaspy


import java.time.LocalDateTime

import ldbc.core.*
import ldbc.core.syntax.given
import ldbc.schemaspy.SchemaSpyGenerator

case class User(
  id:         Long,
  name:       String,
  age:        Option[Int],
  roleId:     Long,
  roleStatus: Long,
  updatedAt:  LocalDateTime,
  createdAt:  LocalDateTime
)

case class Role(id: Long, name: String, status: Long)

object Example extends App:

  val roleTable = Table[Role]("role")(
    column("id", BIGINT(64), AUTO_INCREMENT),
    column("name", VARCHAR(255)),
    column("status", BIGINT(64))
  )
    .keySet(v => PRIMARY_KEY(cats.data.NonEmptyList.of(v.id, v.status)))

  val userTable = Table[User]("user")(
    column("id", BIGINT(64), "ユーザー識別子", AUTO_INCREMENT, PRIMARY_KEY),
    column("name", VARCHAR(255)),
    column("age", INT(255).DEFAULT_NULL),
    column("role_id", BIGINT(64)),
    column("role_status", BIGINT(64)),
    column("updated_at", TIMESTAMP.DEFAULT_CURRENT_TIMESTAMP()),
    column("created_at", TIMESTAMP.DEFAULT_CURRENT_TIMESTAMP(true))
  )
    .keySet(v =>
      CONSTRAINT(
        "fk_id",
        FOREIGN_KEY(v.roleId, REFERENCE(roleTable)(roleTable.id))
      )
    )

  val db = new Database:

    override val databaseType: Database.Type = Database.Type.MySQL

    override val name: String = "ldbc_example"

    override val schema: String = "ldbc_example"

    override val schemaMeta: Option[String] = None

    override val catalog: Option[String] = Some("def")

    override val characterSet: Option[Character] = None

    override val host: String = "127.0.0.1"

    override val port: Int = 13306

    override val tables = Set(roleTable, userTable)

  val file = java.io.File("document")
  SchemaSpyGenerator.default(db, file).generate()
  //SchemaSpyGenerator.connect(db, "takapi327", Some("takapi327"), file).generate()

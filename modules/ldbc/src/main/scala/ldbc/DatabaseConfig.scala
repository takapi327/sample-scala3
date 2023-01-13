
package ldbc

case class DatabaseConfig(
  path:        String,
  database:    String,
  replication: Option[String]
):

  /** Name of database including replication */
  val named: String = database + replication.map(v => "_" + v).getOrElse("")

  override def toString: String = s"$path://$database${ replication.map(v => s"/$v").getOrElse("") }"

object DatabaseConfig:

  private val SYNTAX_DATABASE_CONFIG1 = """^([.\w]+)://(\w+?)$""".r
  private val SYNTAX_DATABASE_CONFIG2 = """^([.\w]+)://(\w+?)/(\w+)$""".r

  def apply(str: String): DatabaseConfig = str match
    case SYNTAX_DATABASE_CONFIG1(path, database)              => DatabaseConfig(path, database, None)
    case SYNTAX_DATABASE_CONFIG2(path, database, replication) => DatabaseConfig(path, database, Some(replication))
    case _ =>
      throw new IllegalArgumentException(
        s"""
           |$str does not match DatabaseConfig format
           |
           |example:
           |  path://database or path://database/replication
           |""".stripMargin
      )

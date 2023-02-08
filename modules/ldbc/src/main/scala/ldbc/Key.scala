
package ldbc

trait Key:

  def queryString: String

case class PrimaryKey[F[_]](column: Column[F]*) extends Key:

  override def queryString: String = if column.nonEmpty then s"PRIMARY KEY(${column.mkString(",")})" else "PRIMARY KEY"

  override def toString: String = "PRIMARY KEY"

case class UniqueKey[F[_]](column: Column[F]*) extends Key:
  override def queryString: String = if column.nonEmpty then s"UNIQUE KEY(${column.mkString(",")})" else "UNIQUE KEY"

  override def toString: String = "UNIQUE KEY"

case class Index() extends Key:
  override def queryString: String = ""

case class ForeignKey() extends Key:
  override def queryString: String = ""

case class AutoInc(key: Option[AutoInc.Key]):
  def queryString: String = key.fold("AUTO_INCREMENT")(v => s"AUTO_INCREMENT $v")

  override def toString: String = queryString

object AutoInc:

  enum Key(label: String):
    case PRIMARY_KEY extends Key("PRIMARY KEY")
    case UNIQUE_KEY  extends Key("UNIQUE KEY")

    override def toString: String = label

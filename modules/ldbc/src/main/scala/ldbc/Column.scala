
package ldbc

trait Column[F[_]]:

  type ThisType

  def label: String

  def `type`: Column.Type

  def options: Seq[ColumnOption]

  def loader: ResultSetLoader[F, ThisType]

  final def get(resultSet: ResultSet[F]): F[ThisType] =
    resultSet.get[ThisType](label)(using loader)

  def isOptional: Boolean = loader.isOptional

  def columnQuery: String =
    s"`$label` ${`type`} ${if isOptional then "DEFAULT NULL" else "NOT NULL"}" ++
      (if options.nonEmpty then s"${options.mkString(" ", " ", "")}" else "")

  override def toString: String = columnQuery

object Column:

  trait Type

  trait IntegerType extends Type:
    def length: Int

  case class Tinyint(length: Int) extends IntegerType:
    override def toString: String = s"TINYINT($length)"

  case class Smallint(length: Int) extends IntegerType:
    override def toString: String = s"SMALLINT($length)"

  case class Mediumint(length: Int) extends IntegerType:
    override def toString: String = s"MEDIUMINT($length)"

  case class CInt(length: Int) extends IntegerType:
    override def toString: String = s"INT($length)"

  case class Bigint(length: Int) extends IntegerType:
    override def toString: String = s"BIGINT($length)"

  case class Decimal(accuracy: Int, scale: Int) extends Type:
    override def toString: String = s"DECIMAL($accuracy, $scale)"

  case class Float(accuracy: Int) extends Type:
    override def toString: String = s"FLOAT($accuracy)"

  case class Bit(length: Int) extends Type:
    override def toString: String = s"BIT($length)"

  case class Character(charset: String, collate: Option[String]):
    override def toString: String = collate.fold(s"CHARACTER SET $charset")(v =>
      s"CHARACTER SET $charset COLLATE $v"
    )

    def setCollate(collate: String): Character =
      this.copy(charset, Some(collate))

  object Character

  trait StringType extends Type:
    def character: Option[Character]

  case class Char(length: Int, character: Option[Character]) extends StringType:

    override def toString: String = character.fold(s"CHAR($length)")(c =>
      s"CHAR($length) $c"
    )

    def setCharacter(character: Character): Char =
      this.copy(length, Some(character))

  case class Varchar(length: Int, character: Option[Character]) extends StringType:

    override def toString: String = character.fold(s"VARCHAR($length)")(c =>
      s"VARCHAR($length) $c"
    )

    def setCharacter(character: Character): Varchar =
      this.copy(length, Some(character))

  case class Binary(length: Int, character: Option[Character]) extends StringType:
    override def toString: String = character.fold(s"BINARY($length)")(c =>
      s"BINARY($length) $c"
    )

    def setCharacter(character: Character): Binary =
      this.copy(length, Some(character))

  case class Varbinary(length: Int, character: Option[Character]) extends StringType:
    override def toString: String = character.fold(s"VARBINARY($length)")(c =>
      s"VARBINARY($length) $c"
    )

    def setCharacter(character: Character): Varbinary =
      this.copy(length, Some(character))

  trait BlobType extends StringType

  case class Tinyblob(character: Option[Character]) extends BlobType:
    override def toString: String = character.fold("TINYBLOB")(c =>
      s"TINYBLOB $c"
    )

    def setCharacter(character: Character): Tinyblob =
      this.copy(Some(character))

  case class Blob(length: Long, character: Option[Character]) extends BlobType:
    override def toString: String = character.fold(s"BLOB($length)")(c =>
      s"BLOB($length) $c"
    )

    def setCharacter(character: Character): Blob =
      this.copy(length, Some(character))

  case class Mediumblob(character: Option[Character]) extends BlobType:
    override def toString: String = character.fold("MEDIUMBLOB")(c =>
      s"MEDIUMBLOB $c"
    )

    def setCharacter(character: Character): Mediumblob =
      this.copy(Some(character))

  trait TextType extends StringType

  case class Tinytext(character: Option[Character]) extends TextType:
    override def toString: String = character.fold("TINYTEXT")(c =>
      s"TINYTEXT $c"
    )

  case class Text(character: Option[Character]) extends TextType:
    override def toString: String = character.fold(s"TEXT")(c =>
      s"TEXT $c"
    )

  case class Mediumtext(character: Option[Character]) extends TextType:
    override def toString: String = character.fold("MEDIUMTEXT")(c =>
      s"MEDIUMTEXT $c"
    )

  case class Longtext(character: Option[Character]) extends TextType:
    override def toString: String = character.fold("LONGTEXT")(c =>
      s"LONGTEXT $c"
    )

  case class Enum(values: List[String]) extends Type:

    override def toString: String = s"ENUM(${values.map(str => s"'$str'").mkString(",")})"

  case class CSet(values: List[String], character: Option[Character]) extends Type:
    override def toString: String = character.fold(s"SET(${values.map(str => s"'$str'").mkString(",")})")(c =>
      s"SET(${values.map(str => s"'$str'").mkString(",")}) $c"
    )

//enum ColumnOption(name: String, columns: String*):
//  case AutoInc extends ColumnOption("AUTO_INCREMENT")
//  case PrimaryKey extends ColumnOption("PRIMARY KEY")
//  case Unique extends ColumnOption("UNIQUE")
//
//  override def toString: String = name

trait ColumnOption:
  def name: String

object ColumnOption:

  case object AutoInc extends ColumnOption:
    override def name: String = "AUTO_INCREMENT"

    override def toString: String = name

  case class PrimaryKey(columns: String*) extends ColumnOption:
    override def name: String = "PRIMARY KEY" ++ (if columns.nonEmpty then s" (${columns.mkString(", ")})" else "")

    override def toString: String = name

  case class Unique(columns: String*) extends ColumnOption:
    override def name: String = "UNIQUE" ++ (if columns.nonEmpty then s" (${columns.mkString(", ")})" else "")

    override def toString: String = name

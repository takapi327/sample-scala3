
package ldbc

trait Column[F[_]]:

  type ThisType

  def label: String

  def `type`: Column.Type

  def loader: ResultSetLoader[F, ThisType]

  final def get(resultSet: ResultSet[F]): F[ThisType] =
    resultSet.get[ThisType](label)(using loader)

  override def toString: String = s"$label ${`type`}"

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

  case class Text(length: Int, character: Option[Character]) extends TextType:
    override def toString: String = character.fold(s"TEXT($length)")(c =>
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

  case class Enum(values: cats.data.NonEmptyList[String]) extends Type:

    override def toString: String = s"ENUM(${values.toList.map(str => s"'$str'").mkString(",")})"

  case class CSet(values: cats.data.NonEmptyList[String], character: Option[Character]) extends Type:
    override def toString: String = character.fold(s"SET(${values.toList.map(str => s"'$str'").mkString(",")})")(c =>
      s"SET(${values.toList.map(str => s"'$str'").mkString(",")}) $c"
    )

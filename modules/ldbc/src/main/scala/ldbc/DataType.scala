
package ldbc

import java.time.*
import java.time.Year as JYear

sealed trait DataType[T]:

  def columQuery: String

  def isOptional: Boolean = false

  protected val nullType: String = if isOptional then "DEFAULT NULL" else "NOT NULL"

  override def toString: String = columQuery

object DataType:

  case class Character(charset: String, collate: Option[String]):
    override def toString: String = collate.fold(s"CHARACTER SET $charset")(v =>
      s"CHARACTER SET $charset COLLATE $v"
    )

    def setCollate(collate: String): Character =
      this.copy(charset, Some(collate))

  sealed trait IntegerType[T <: Byte | Short | Int | Long | Float | Double | BigDecimal | Any] extends DataType[T]:
    def length: Int
    def default: Option[Default]
  sealed trait IntegerOptType[T <: Option[Byte | Short | Int | Long | Float | Double | BigDecimal]] extends DataType[T]:
    def length: Int
    def default: Option[Default]

  sealed trait StringType[T <: Byte | Array[Byte] | String] extends DataType[T]:
    def character: Option[Character]

  sealed trait StringOptType[T <: Option[Byte | Array[Byte] | String]] extends DataType[T]:
    def character: Option[Character]

    override def isOptional: Boolean = true

  sealed trait BlobType[T <: Array[Byte]] extends DataType[T]
  sealed trait BlobOptType[T <: Option[Array[Byte]]] extends DataType[T]:
    override def isOptional: Boolean = true

  sealed trait DateType[T <: Instant | OffsetTime | LocalTime | LocalDate | LocalDateTime | OffsetDateTime | ZonedDateTime | JYear] extends DataType[T]
  sealed trait DateOptType[T <: Option[Instant | OffsetTime | LocalTime | LocalDate | LocalDateTime | OffsetDateTime | ZonedDateTime | JYear]] extends DataType[T]:
    override def isOptional: Boolean = true

  /** List of Numeric Data Types */

  case class Bit[T <: Byte | Short | Int | Long | Float | Double | BigDecimal](
    length:  Int,
    default: Option[Default]
  ) extends IntegerType[T]:
    override def columQuery: String = s"BIT($length) $nullType" ++ default.fold("")(v => s" ${v.queryString}")
    def DEFAULT(value: T): Bit[T] = this.copy(length, Some(DefaultValue(value)))
  case class BitOpt[T <: Option[Byte | Short | Int | Long | Float | Double | BigDecimal]](
    length: Int,
    default: Option[Default]
  ) extends IntegerOptType[T]:
    override def columQuery: String = s"BIT($length) $nullType" ++ default.fold("")(v => s" ${v.queryString}")
    def DEFAULT(value: T): BitOpt[T] = this.copy(length, Some(DefaultValue(value)))

  case class Tinyint[T <: Byte | Any](length: Int, default: Option[Default], autoInc: Option[AutoInc]) extends IntegerType[T]:
    override def columQuery: String = s"TINYINT($length) $nullType" ++ default.fold("")(v => s" ${v.queryString}") ++ autoInc.fold("")(v => s" ${v.queryString}")
    def DEFAULT(value: T): Tinyint[T] = this.copy(length, Some(DefaultValue(value)))

    def AUTO_INCREMENT(key: AutoInc.Key): Tinyint[T] = this.copy(autoInc = Some(AutoInc(Some(key))))

    def updateAutoInc(autoInc: Option[AutoInc]): DataType[T] =
      this.copy(autoInc = autoInc)

  case class TinyintOpt[T <: Option[Byte]](length: Int, default: Option[Default]) extends IntegerOptType[T]:
    override def columQuery: String = s"TINYINT($length) $nullType" ++ default.fold("")(v => s" ${v.queryString}")
    def DEFAULT(value: T): TinyintOpt[T] = this.copy(length, Some(DefaultValue(value)))

  case class Smallint[T <: Short](length: Int, default: Option[Default]) extends IntegerType[T]:
    override def columQuery: String = s"SMALLINT($length) $nullType" ++ default.fold("")(v => s" ${v.queryString}")
    def DEFAULT(value: T): Smallint[T] = this.copy(length, Some(DefaultValue(value)))
  case class SmallintOpt[T <: Option[Short]](length: Int, default: Option[Default]) extends IntegerOptType[T]:
    override def columQuery: String = s"SMALLINT($length) $nullType" ++ default.fold("")(v => s" ${v.queryString}")
    def DEFAULT(value: T): SmallintOpt[T] = this.copy(length, Some(DefaultValue(value)))

  case class Mediumint[T <: Int](length: Int, default: Option[Default]) extends IntegerType[T]:
    override def columQuery: String = s"MEDIUMINT($length) $nullType" ++ default.fold("")(v => s" ${v.queryString}")
    def DEFAULT(value: T): Mediumint[T] = this.copy(length, Some(DefaultValue(value)))
  case class MediumintOpt[T <: Option[Int]](length: Int, default: Option[Default]) extends IntegerOptType[T]:
    override def columQuery: String = s"MEDIUMINT($length) $nullType" ++ default.fold("")(v => s" ${v.queryString}")
    def DEFAULT(value: T): MediumintOpt[T] = this.copy(length, Some(DefaultValue(value)))

  case class Integer[T <: Int](length: Int, default: Option[Default]) extends IntegerType[T]:
    override def columQuery: String = s"INT($length) $nullType" ++ default.fold("")(v => s" ${v.queryString}")
    def DEFAULT(value: T): Integer[T] = this.copy(length, Some(DefaultValue(value)))
  case class IntegerOpt[T <: Option[Int]](length: Int, default: Option[Default]) extends IntegerOptType[T]:
    override def columQuery: String = s"INT($length) $nullType" ++ default.fold("")(v => s" ${v.queryString}")
    def DEFAULT(value: T): IntegerOpt[T] = this.copy(length, Some(DefaultValue(value)))

  case class Bigint[T <: Long](length: Int, default: Option[Default]) extends IntegerType[T]:
    override def columQuery: String = s"BIGINT($length) $nullType" ++ default.fold("")(v => s" ${v.queryString}")
    def DEFAULT(value: T): Bigint[T] = this.copy(length, Some(DefaultValue(value)))
  case class BigintOpt[T <: Option[Long]](length: Int, default: Option[Default]) extends IntegerOptType[T]:
    override def columQuery: String = s"BIGINT($length) $nullType" ++ default.fold("")(v => s" ${v.queryString}")
    def DEFAULT(value: T): BigintOpt[T] = this.copy(length, Some(DefaultValue(value)))

  case class Decimal[T <: BigDecimal](accuracy: Int, scale: Int, default: Option[Default]) extends DataType[T]:
    override def columQuery: String = s"DECIMAL($accuracy, $scale) $nullType" ++ default.fold("")(v => s" ${v.queryString}")
    def DEFAULT(value: T): Decimal[T] = this.copy(accuracy, scale, Some(DefaultValue(value)))
  case class DecimalOpt[T <: Option[BigDecimal]](accuracy: Int, scale: Int, default: Option[Default]) extends DataType[T]:
    override def columQuery: String = s"DECIMAL($accuracy, $scale) $nullType" ++ default.fold("")(v => s" ${v.queryString}")
    override def isOptional: Boolean = true
    def DEFAULT(value: T): DecimalOpt[T] = this.copy(accuracy, scale, Some(DefaultValue(value)))

  case class CFloat[T <: Double | Float](accuracy: Int, default: Option[Default]) extends DataType[T]:
    override def columQuery: String = s"FLOAT($accuracy) $nullType" ++ default.fold("")(v => s" ${v.queryString}")
    def DEFAULT(value: T): CFloat[T] = this.copy(accuracy, Some(DefaultValue(value)))
  case class CFloatOpt[T <: Option[Double | Float]](accuracy: Int, default: Option[Default]) extends DataType[T]:
    override def columQuery: String = s"FLOAT($accuracy) $nullType" ++ default.fold("")(v => s" ${v.queryString}")
    override def isOptional: Boolean = true
    def DEFAULT(value: T): CFloatOpt[T] = this.copy(accuracy, Some(DefaultValue(value)))

  /** List of String Data Types */

  case class CChar[T <: String](length: Int, default: Option[Default], character: Option[Character]) extends StringType[T]:
    override def columQuery: String = s"CHAR($length) $nullType" ++ default.fold("")(v => s" ${v.queryString}") ++ character.fold("")(v => s" $v")
    def DEFAULT(value: T): CChar[T] = this.copy(length, Some(DefaultValue(value)), character)
  case class CCharOpt[T <: Option[String]](length: Int, default: Option[Default], character: Option[Character]) extends StringOptType[T]:
    override def columQuery: String = s"CHAR($length) $nullType" ++ default.fold("")(v => s" ${v.queryString}") ++ character.fold("")(v => s" $v")
    def DEFAULT(value: T): CCharOpt[T] = this.copy(length, Some(DefaultValue(value)), character)

  case class Varchar[T <: String](length: Int, default: Option[Default], character: Option[Character]) extends StringType[T]:
    override def columQuery: String = s"VARCHAR($length) $nullType" ++ default.fold("")(v => s" ${v.queryString}") ++ character.fold("")(v => s" $v")
    def DEFAULT(value: T): Varchar[T] = this.copy(length, Some(DefaultValue(value)), character)
  case class VarcharOpt[T <: Option[String]](length: Int, default: Option[Default], character: Option[Character]) extends StringOptType[T]:
    override def columQuery: String = s"VARCHAR($length) $nullType" ++ default.fold("")(v => s" ${v.queryString}") ++ character.fold("")(v => s" $v")
    def DEFAULT(value: T): VarcharOpt[T] = this.copy(length, Some(DefaultValue(value)), character)

  case class Binary[T <: Array[Byte]](length: Int, character: Option[Character]) extends StringType[T]:
    override def columQuery: String = character.fold(s"BINARY($length) $nullType")(c => s"BINARY($length) $nullType $c")
  case class BinaryOpt[T <: Option[Array[Byte]]](length: Int, character: Option[Character]) extends StringOptType[T]:
    override def columQuery: String = character.fold(s"BINARY($length) $nullType")(c => s"BINARY($length) $nullType $c")

  case class Varbinary[T <: Array[Byte]](length: Int, character: Option[Character]) extends StringType[T]:
    override def columQuery: String = character.fold(s"VARBINARY($length) $nullType")(c => s"VARBINARY($length) $nullType $c")
  case class VarbinaryOpt[T <: Option[Array[Byte]]](length: Int, character: Option[Character]) extends StringOptType[T]:
    override def columQuery: String = character.fold(s"VARBINARY($length) $nullType")(c => s"VARBINARY($length) $nullType $c")

  case class Tinyblob[T <: Array[Byte]](character: Option[Character]) extends BlobType[T]:
    override def columQuery: String = character.fold(s"TINYBLOB $nullType")(c => s"TINYBLOB $nullType $c")
  case class TinyblobOpt[T <: Option[Array[Byte]]](character: Option[Character]) extends BlobOptType[T]:
    override def columQuery: String = character.fold(s"TINYBLOB $nullType")(c => s"TINYBLOB $nullType $c")

  case class Blob[T <: Array[Byte]](length: Long, character: Option[Character]) extends BlobType[T]:
    override def columQuery: String = character.fold(s"BLOB($length) $nullType")(c => s"BLOB($length) $nullType $c")
  case class BlobOpt[T <: Option[Array[Byte]]](length: Long, character: Option[Character]) extends BlobOptType[T]:
    override def columQuery: String = character.fold(s"BLOB($length) $nullType")(c => s"BLOB($length) $nullType $c")

  case class Mediumblob[T <: Array[Byte]](character: Option[Character]) extends BlobType[T]:
    override def columQuery: String = character.fold(s"MEDIUMBLOB $nullType")(c => s"MEDIUMBLOB $nullType $c")
  case class MediumblobOpt[T <: Option[Array[Byte]]](character: Option[Character]) extends BlobOptType[T]:
    override def columQuery: String = character.fold(s"MEDIUMBLOB $nullType")(c => s"MEDIUMBLOB $nullType $c")

  case class LongBlob[T <: Array[Byte]](character: Option[Character]) extends BlobType[T]:
    override def columQuery: String = character.fold(s"LONGBLOB $nullType")(c => s"LONGBLOB $nullType $c")
  case class LongBlobOpt[T <: Option[Array[Byte]]](character: Option[Character]) extends BlobOptType[T]:
    override def columQuery: String = character.fold(s"LONGBLOB $nullType")(c => s"LONGBLOB $nullType $c")

  case class TinyText[T <: String](character: Option[Character]) extends StringType[T]:
    override def columQuery: String = character.fold(s"TINYTEXT $nullType")(c => s"TINYTEXT $nullType $c")
  case class TinyTextOpt[T <: Option[String]](character: Option[Character]) extends StringOptType[T]:
    override def columQuery: String = character.fold(s"TINYTEXT $nullType")(c => s"TINYTEXT $nullType $c")

  case class Text[T <: String](character: Option[Character]) extends StringType[T]:
    override def columQuery: String = character.fold(s"TEXT $nullType")(c => s"TEXT $nullType $c")
  case class TextOpt[T <: Option[String]](character: Option[Character]) extends StringOptType[T]:
    override def columQuery: String = character.fold(s"TEXT $nullType")(c => s"TEXT $nullType $c")

  case class MediumText[T <: String](character: Option[Character]) extends StringType[T]:
    override def columQuery: String = character.fold(s"MEDIUMTEXT $nullType")(c => s"MEDIUMTEXT $nullType $c")
  case class MediumTextOpt[T <: Option[String]](character: Option[Character]) extends StringOptType[T]:
    override def columQuery: String = character.fold(s"MEDIUMTEXT $nullType")(c => s"MEDIUMTEXT $nullType $c")

  case class LongText[T <: String](character: Option[Character]) extends StringType[T]:
    override def columQuery: String = character.fold(s"LONGTEXT $nullType")(c => s"LONGTEXT $nullType $c")
  case class LongTextOpt[T <: Option[String]](character: Option[Character]) extends StringOptType[T]:
    override def columQuery: String = character.fold(s"LONGTEXT $nullType")(c => s"LONGTEXT $nullType $c")

  /** List of Date Data Types */

  case class Date[T <: LocalDate](default: Option[Default]) extends DateType[T]:
    override def columQuery: String = s"DATE $nullType" ++ default.fold("")(v => s" ${v.queryString}")
    def DEFAULT(value: T): Date[T] = this.copy(Some(DefaultValue(value)))
  case class DateOpt[T <: Option[LocalDate]](default: Option[Default]) extends DateOptType[T]:
    override def columQuery: String = s"DATE $nullType" ++ default.fold("")(v => s" ${v.queryString}")
    def DEFAULT(value: T): DateOpt[T] = this.copy(Some(DefaultValue(value)))

  case class DateTime[T <: Instant | LocalDateTime](default: Option[Default]) extends DateType[T]:
    override def columQuery: String = s"DATETIME $nullType" ++ default.fold("")(v => s" ${v.queryString}")
    def DEFAULT(value: T): DateTime[T] = this.copy(Some(DefaultValue(value)))
    def DEFAULT_CURRENT_TIMESTAMP(onUpdate: Boolean = false): DateTime[T] = this.copy(Some(DefaultTimeStamp(onUpdate)))
  case class DateTimeOpt[T <: Option[Instant | LocalDateTime]](default: Option[Default]) extends DateOptType[T]:
    override def columQuery: String = s"DATETIME $nullType" ++ default.fold("")(v => s" ${v.queryString}")
    def DEFAULT(value: T): DateTimeOpt[T] = this.copy(Some(DefaultValue(value)))
    def DEFAULT_CURRENT_TIMESTAMP(onUpdate: Boolean = false): DateTimeOpt[T] = this.copy(Some(DefaultTimeStamp(onUpdate)))

  case class TimeStamp[T <: Instant | LocalDateTime](default: Option[Default]) extends DateType[T]:
    override def columQuery: String = s"TIMESTAMP $nullType" ++ default.fold("")(v => s" ${v.queryString}")
    def DEFAULT(value: T): TimeStamp[T] = this.copy(Some(DefaultValue(value)))
    def DEFAULT_CURRENT_TIMESTAMP(onUpdate: Boolean = false): TimeStamp[T] = this.copy(Some(DefaultTimeStamp(onUpdate)))
  case class TimeStampOpt[T <: Option[Instant | LocalDateTime]](default: Option[Default]) extends DateOptType[T]:
    override def columQuery: String = s"TIMESTAMP $nullType" ++ default.fold("")(v => s" ${v.queryString}")
    def DEFAULT(value: T): TimeStampOpt[T] = this.copy(Some(DefaultValue(value)))
    def DEFAULT_CURRENT_TIMESTAMP(onUpdate: Boolean = false): TimeStampOpt[T] = this.copy(Some(DefaultTimeStamp(onUpdate)))

  case class Time[T <: LocalTime](default: Option[Default]) extends DateType[T]:
    override def columQuery: String = s"TIME $nullType" ++ default.fold("")(v => s" ${v.queryString}")
    def DEFAULT(value: T): Time[T] = this.copy(Some(DefaultValue(value)))
  case class TimeOpt[T <: Option[LocalTime]](default: Option[Default]) extends DateOptType[T]:
    override def columQuery: String = s"TIME $nullType" ++ default.fold("")(v => s" ${v.queryString}")
    def DEFAULT(value: T): TimeOpt[T] = this.copy(Some(DefaultValue(value)))

  case class Year[T <: Instant | LocalDate | JYear](default: Option[Default]) extends DateType[T]:
    override def columQuery: String = s"YEAR $nullType" ++ default.fold("")(v => s" ${v.queryString}")
    def DEFAULT(value: T): Year[T] = this.copy(Some(DefaultValue(value)))
  case class YearOpt[T <: Option[Instant | LocalDate | JYear]](default: Option[Default]) extends DateOptType[T]:
    override def columQuery: String = s"YEAR $nullType" ++ default.fold("")(v => s" ${v.queryString}")
    def DEFAULT(value: T): YearOpt[T] = this.copy(Some(DefaultValue(value)))

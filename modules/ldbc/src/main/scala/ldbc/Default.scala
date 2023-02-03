
package ldbc

import java.time.{ Instant, LocalDateTime }

trait Default:
  def queryString: String

  override def toString: String = queryString

object Default:
  
  object Null extends Default:
    override def queryString: String = "DEFAULT NULL"

trait DefaultValue[T] extends Default:

  def value: T

  override def queryString: String = s"DEFAULT '$value'"

object DefaultValue:

  def apply[T](_value: T): Default = new DefaultValue[T]:
    override def value: T = _value

trait DefaultTimeStamp extends Default:

  val withOn: Boolean = false

  override def queryString: String =
    if withOn then "DEFAULT CURRENT_TIMESTAMP"
    else "DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"

object DefaultTimeStamp:
  def apply(): DefaultTimeStamp =
    new DefaultTimeStamp {}
  def apply(_withOn: Boolean = false): DefaultTimeStamp =
    new DefaultTimeStamp:
      override val withOn: Boolean = _withOn

//package library.rdb
/*
trait JdbcUrlBuilder[+A]

case class JdbcUrl[T]() extends JdbcUrlBuilder[T]

import org.atnos.eff.*
import org.atnos.eff.addon.doobie.DoobieConnectionIOEffect.*
import org.atnos.eff.interpret.*

trait JdbcUrlBuilderCreator:

  type _jdbcUrlBuilder[R] = JdbcUrlBuilder |= R

  def buildJdbcUrl[T, R: _jdbcUrlBuilder](): Eff[R, T] =
    EffCreation.send(JdbcUrl())

object JdbcUrlBuilderCreator extends JdbcUrlBuilderCreator

trait MySqlJdbcUrlBuilderInterpretation:

  def buildJdbcUrl[R, U, A](eff: Eff[R, A])(
    using member: Member.Aux[JdbcUrlBuilder, R, U]
  ): Eff[R, A] =
    translate(eff)(new Translate[JdbcUrlBuilder, U] {
      def apply[X](kv: JdbcUrlBuilder[X]): Eff[U, X] = kv match
        case JdbcUrl() => ()
    })

object MySqlJdbcUrlBuilderInterpretation extends MySqlJdbcUrlBuilderInterpretation

trait MariaDBJdbcUrlBuilderInterpretation:
  def buildJdbcUrl[R, U, A](eff: Eff[R, A])(
    using member: Member.Aux[JdbcUrlBuilder, R, U]
  ): Eff[R, A] =
    translate(eff)(new Translate[JdbcUrlBuilder, U] {
      def apply[X](kv: JdbcUrlBuilder[X]): Eff[U, X] = kv match
        case JdbcUrl() => ()
    })

object MariaDBJdbcUrlBuilderInterpretation extends MariaDBJdbcUrlBuilderInterpretation

trait Syntax {
  given toMySqlJdbcUrlBuilder[R, A](
    eff: Eff[R, A]
  ): MySqlJdbcUrlBuilder[R, A] = new MySqlJdbcUrlBuilder[R, A](eff)

  given toMariaDBJdbcUrlBuilder[R, A](
    eff: Eff[R, A]
  ): MariaDBJdbcUrlBuilder[R, A] = new MariaDBJdbcUrlBuilder[R, A](eff)
}

object Syntax extends Syntax

final class MySqlJdbcUrlBuilder[R, A](private val eff: Eff[R, A])
  extends AnyVal:

  def buildJdbcUrl[U](
    using member: Member.Aux[JdbcUrlBuilder, R, U]
  ): Eff[U, A] = MySqlJdbcUrlBuilderInterpretation.buildJdbcUrl(eff)

final class MariaDBJdbcUrlBuilder[R, A](private val eff: Eff[R, A])
  extends AnyVal:

  def buildJdbcUrl[U](
    using member: Member.Aux[JdbcUrlBuilder, R, U]
  ): Eff[U, A] = MariaDBJdbcUrlBuilder.buildJdbcUrl(eff)
*/
package domain.value

import scala.util.matching.Regex

import Refine.*

object Email:

  lazy val emailPatturn: Regex = """[\w\-._]+@[\w\-._]+\.[A-Za-z]+""".r

  opaque type InternetAddress = String

  def apply(str: String): InternetAddress =
    str

  def patturnMatch(email: String): Either[String, InternetAddress] =
    emailPatturn.matches(email) match
      case true  => Right(email)
      case false => Left("Could not match the regular expression pattern")

  def patturnMatch2(email: String): InternetAddress =
    emailPatturn.matches(email) match
      case true  => email
      case false => throw new IllegalArgumentException(s"Could not match the regular expression pattern ${email}")

  def refineTest(x: String): Either[String, InternetAddress] =
    refineV(x)

  extension (email: InternetAddress)
    def matchRegex: Either[String, InternetAddress] = patturnMatch(email)
    def matchRegex2: InternetAddress = patturnMatch2(email)
    def refine: Either[String, InternetAddress] = refineTest(email)

end Email

trait RefineType
  //val validate

object EmailRefineType extends RefineType:
  val validate: Regex = """[\w\-._]+@[\w\-._]+\.[A-Za-z]+""".r

trait Refine[T]:
  def patturnMatch(x: T): Either[String, T]

given emailRefine: Refine[String] with
  opaque type Refine = String
  lazy val emailPatturn: Regex = """[\w\-._]+@[\w\-._]+\.[A-Za-z]+""".r
  def patturnMatch(x: String): Either[String, Refine] =
    emailPatturn.matches(x) match
      case true  => Right(x)
      case false => Left("Could not match the regular expression pattern")

object Refine:
  def refineV[T](x: T)(using refine: Refine[T]): Either[String, T] =
    refine.patturnMatch(x)
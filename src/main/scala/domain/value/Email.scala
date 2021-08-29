package domain.value

import scala.util.matching.Regex

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

  extension (email: InternetAddress)
    def matchRegex: Either[String, InternetAddress] = patturnMatch(email)
    def matchRegex2: InternetAddress = patturnMatch2(email)

end Email

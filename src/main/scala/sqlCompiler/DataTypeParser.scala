
import scala.util.parsing.combinator.*
import scala.util.parsing.input.*

trait DataTypeParser:
  self: RegexParsers =>

  // デジット（0以上の数字）のパーサー
  private def digit: Parser[Int] = """\d+""".r ^^ (_.toInt)

  def customError[A](parser: Parser[A], msg: String): Parser[A] = Parser[A] { input =>
    parser(input) match
      case Failure(_, in) => Failure(msg, in)
      case result => result
  }

  private def unsigned: Parser[String] = "(?i)unsigned".r ^^ (_.toUpperCase)
  private def zerofill: Parser[String] = "(?i)zerofill".r ^^ (_.toUpperCase)
  
  protected def dataType: Parser[String] = bitType | tinyintType | characterType

  /**
   * ==========================================
   * Numeric data type parsing
   * ==========================================
   */
  private def bitType: Parser[String] =
    customError(
      "(?i)bit".r ~> "(" ~> digit.filter(n => n >= 1 && n <= 64) <~ ")" ^^ { n =>
        s"BIT($n)"
      },
      """
        |===============================================================================
        |Failed to parse Bit data type.
        |The Bit Data type must be defined as follows
        |※ Bit strings are case-insensitive.
        |
        |M is the number of bits per value (1 to 64). If M is omitted, the default is 1.
        |
        |SEE: https://man.plustar.jp/mysql/numeric-type-syntax.html
        |
        |example: BIT[(M)]
        |==============================================================================
        |""".stripMargin
    )

  private def tinyintType: Parser[String] =
    customError(
      "(?i)tinyint".r ~> "(" ~> digit.filter(n => n >= -128 && n <= 255) ~ ")" ~ opt(unsigned) ~ opt(zerofill) ^^ {
        case n ~ _ ~ unsigned ~ zerofill => s"BIT($n)" + unsigned.fold("")(v => s" $v") + zerofill.fold("")(v => s" $v")
      },
      """
        |===============================================================================
        |Failed to parse tinyint data type.
        |The tinyint Data type must be defined as follows
        |※ tinyint strings are case-insensitive.
        |
        |M, the signed range is -128 to 127. The unsigned range is 0 to 255.
        |
        |SEE: https://man.plustar.jp/mysql/numeric-type-syntax.html
        |
        |example: TINYINT[(M)] [UNSIGNED] [ZEROFILL]
        |==============================================================================
        |""".stripMargin
    )

  private def characterType: Parser[String] =
    customError(
      "(?i)character".r ~> "(" ~> digit.filter(n => n >= 0 && n <= 255) <~ ")" ^^ { n =>
        s"CHARACTER($n)"
      },
      ""
    )

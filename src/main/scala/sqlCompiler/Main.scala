import java.io.File
import java.nio.file.Files
import java.nio.charset.Charset
import java.nio.charset.MalformedInputException

import scala.io.Codec
import scala.util.matching.Regex
import scala.util.parsing.combinator.*
import scala.util.parsing.input.*

object SqlCompiler:

  def main(args: Array[String]): Unit =

    val sqlContent = new String(Files.readAllBytes(new File("/Users/takapi327/Development/scala-app/sample-scala3/src/main/scala/sqlCompiler/test.sql").toPath), Charset.defaultCharset())

    CreateParser.parseCreate(sqlContent) match
      case CreateParser.Success(_, _) => println("Success")
      case CreateParser.NoSuccess(errorMessage, _) => println(s"NoSuccess: $errorMessage")
      case CreateParser.Failure(errorMessage, _) => println(s"Failure: $errorMessage")
      case CreateParser.Error(errorMessage, _) => println(s"Error: $errorMessage")

// CREATE文の構文解析器を定義する
object CreateParser extends RegexParsers, JavaTokenParsers:

  // 大文字小文字を無視するパーサー
  override protected val whiteSpace: Regex = """(\s|(?i)//.*)+""".r

  def end: util.matching.Regex = """\s*""".r

  case class Comment(message: String)
  // コメント
  def comment: Parser[Comment] = "/" ~> "*" ~> ident <~ "*" <~ "/" ^^ Comment.apply

  // デジット（0以上の数字）のパーサー
  def digit: Parser[Int] = """\d+""".r ^^ (_.toInt)

  // キーワード
  def create: Parser[String] = "CREATE" ^^ (_.toUpperCase)
  def temporary: Parser[String] = "TEMPORARY" ^^ (_.toUpperCase)
  def table: Parser[String] = "TABLE" ^^ (_.toUpperCase)
  def ifNotExists: Parser[String] = "IF NOT EXISTS" ^^ (_.toUpperCase)

  // 制約キーワードのルール
  def constraintKeyword: Parser[String] =
    ("NOT" | "not") ~> ("NULL" | "null") ^^ (_ => "NOT NULL") | "NULL"

  def characterType: Parser[String] =
    ("character" | "CHARACTER") ~> "(" ~> digit.filter(n => n >= 0 && n <= 255) <~ ")" ^^ { n =>
      s"character($n)"
    }

  // データ型
  def dataType: Parser[String] = "VARCHAR" | "INT" | "FLOAT" | characterType | "text" | "INTEGER" | "NUMERIC" // 必要なデータ型を追加

  // カラム定義
  def columnDef: Parser[(String, String, Option[String])] = ident ~ dataType ~ opt(constraintKeyword) ^^ {
    case columnName ~ dataType ~ constraint => (columnName, dataType, constraint)
  }

  // CREATE文の解析
  def createStatement: Parser[CreateStatement] =
    create ~ opt(temporary) ~ table ~ opt(ifNotExists) ~ ident ~ "(" ~ repsep(columnDef, ",") <~ ")" ~ ";" ^^ {
      case _ ~ _ ~ _ ~ _ ~ tableName ~ _ ~ columnDefs => CreateStatement(tableName, columnDefs)
    }

  def sentence: Parser[Product] = Seq[Parser[Product]](comment, createStatement).reduceLeft(_ | _)

  case class CreateStatement(
    tableName: String,
    columnDefinitions: List[(String, String, Option[String])]
  )

  def parser: Parser[Unit] =
    phrase(rep(sentence) <~ end) ^^ {
      case statements =>
        statements.foldLeft(List[CreateStatement]()) {
          case (list, statement: CreateStatement) =>
            println(s"Table name: ${statement.tableName}")
            statement.columnDefinitions.foreach { case (columnName, dataType, constraint) =>
              val constraintString = constraint.getOrElse("NULL")
              println(s"  Column name: $columnName, Data type: $dataType, Constraint: $constraintString")
            }
            list :+ statement
          case (list, _) => list
        }
    }

  // パース関数
  def parseCreate(sql: String): ParseResult[Unit] =
    parser(new CharSequenceReader(sql))

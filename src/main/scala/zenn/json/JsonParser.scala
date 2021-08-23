package zenn.json

import scala.collection.mutable.Builder
import java.lang.StringBuilder

object JsonParser {
  import Json._

  private type TokenSource = String
  private val unexpectedTermination = Left("unexpected termination")

  def parse(json: String): Either[String, Json] = ???

  final def expectValue(stream: TokenSource, position: Int = 0): Either[String, (Int, Json)] = {
    if (position >= stream.length) unexpectedTermination
    else stream(position) match {
      case '{' => expectObject(stream, position + 1)
      case 't' if stream.startsWith("true", position) =>
        Right((position + 4, JBool(true)))
      case 'f' if stream.startsWith("false", position) =>
        Right((position + 5), JBool(false))
      case 'n' if stream.startsWith("null", position) =>
        Right((position + 4), JNull)
      case _   => ???
    }
  }

  private def expectObject(
    stream:   TokenSource,
    position: Int,
    first:    Boolean = true,
    fields:   JsonObjectBuilder = new JsonObjectBuilder()
  ): Either[String, (Int, Json)] = {
    if (position >= stream.length) unexpectedTermination
    else stream(position) match {
      case '}'               => Right(position + 1, fields.build())
      case ' ' | '\r' | '\n' => expectObject(stream, position + 1, first, fields)
      case _   => ???
    }
  }

  private def expectedStringBounds(stream: TokenSource, position: Int) =
    expectedSpacerToken(stream, position, '"', "Expected string bounds")

  private def expectedSpacerToken(
    stream:   TokenSource,
    position: Int,
    token:    Char,
    message:  String
  ): Either[String, Int] = {
    if (position >= stream.length) unexpectedTermination
    else stream(position) match {
      case ' ' | '\r' | '\n' | '\t' => expectedSpacerToken(stream, position + 1, token, message)
      case `token`                  => Right(position + 1)
      case _                        => Left(message)
    }
  }

  private def expectStringNoStartBounds(
    stream:   TokenSource,
    position: Int,
  ): Either[String, (Int, String)] = {
    collectStringParts(stream, position).map((pos, sb) => (pos, sb.toString))
  }

  private def collectStringParts(
    stream:        TokenSource,
    position:      Int,
    workingString: StringBuilder = new StringBuilder()
  ): Either[String, (Int, StringBuilder)] = {
    def unsafeNormalCharIndex(idx: Int): Int = {
      val char = stream(idx)
      if (char == '"') idx
      else unsafeNormalCharIndex(idx + 1)
    }
    if (position >= stream.length) unexpectedTermination
    else stream(position) match {
      case '"'   => Right(position + 1, workingString)
      case other => {
        val normalCharEnd = unsafeNormalCharIndex(position)
        collectStringParts(
          stream,
          normalCharEnd,
          workingString.append(stream, position, normalCharEnd)
        )
      }
    }
  }

  private[this] final def expectFieldSeparator(
    stream:   TokenSource,
    position: Int,
  ) = expectedSpacerToken(stream, position, ':', "Expected field separator token")

  private case class JsonObjectBuilder(
    val fieldsMapBuilder: Builder[(JsonField, Json), Map[JsonField, Json]] = Map.newBuilder
  ) {
    private var isEmpty: Boolean = true
    def add(key: JsonField, value: Json): JsonObjectBuilder = {
      isEmpty = false
      fieldsMapBuilder += ((key, value))
      this
    }

    def build(): Json = {
      if (isEmpty) Json()
      else JObject(JsonObjectInstance(fieldsMapBuilder.result()))
    }
  }
}

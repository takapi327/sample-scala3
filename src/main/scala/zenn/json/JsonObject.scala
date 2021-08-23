package zenn.json

import zenn.json.Json._

sealed abstract class JsonObject {
  def apply(f: JsonField): Option[Json]
  def +(f: JsonField, j: Json): JsonObject
  def :+(pair: (JsonField, Json)): JsonObject
  def map(f: Json => Json): JsonObject
}

private case class JsonObjectInstance(fieldMap: Map[JsonField, Json] = Map.empty) extends JsonObject {
  def apply(f: JsonField): Option[Json] = fieldMap.get(f)

  def +(key: JsonField, value: Json): JsonObject = {
    copy(fieldMap = fieldMap.updated(key, value))
  }

  def :+(pair: (JsonField, Json)): JsonObject = {
    this.+(pair._1, pair._2)
  }

  def map(f: Json => Json): JsonObject = ???
}

object JsonObject extends JsonObjects {
  def empty: JsonObject = JsonObjectInstance()
  def single(f: JsonField, j: Json): JsonObject = JsonObject.empty + (f, j)
  def fromIterable(js: Iterable[(JsonField, Json)]) = js.foldLeft(empty)(_ :+ _)
}

trait JsonObjects

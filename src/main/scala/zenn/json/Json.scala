package zenn.json

abstract class Json extends Product with Serializable

private case class JNumber(n: MyJsons.MyJsonNumber) extends Json
private case class JString(s: String) extends Json
private case class JBool(b: Boolean) extends Json
private case object JNull extends Json
private case class JObject(o: JsonObject) extends Json

object Json extends Jsons {
  def apply(fields: (JsonField, Json)*): Json = {
    jObjectAssciationList(fields.toList)
  }
}

trait Jsons {

  type JsonBoolean         = Boolean
  type JsonArray           = List[Json]
  type JsonString          = String
  type JsonField           = String
  type JsonAssosiation     = (JsonField, Json)
  type JsonAssosiationList = List[JsonAssosiation]

  def jObjectAssciationList(js: JsonAssosiationList): Json = {
      JObject(JsonObject.fromIterable(js))
  }
}

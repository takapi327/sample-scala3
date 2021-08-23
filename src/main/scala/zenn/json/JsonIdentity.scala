package zenn.json

class JsonIdentity[J](val j: J) {
  extension [T <: Int | Float | Double : EncodeJsonNumber](j: T) {
    def asJsonNumber = summon[EncodeJsonNumber[T]].encodeJsonNumber(j)
  }
}

object JsonIdentity extends JsonIdentitys
trait JsonIdentitys {
  given[J]: Conversion[J, JsonIdentity[J]] with
    def apply(j: J): JsonIdentity[J] = new JsonIdentity(j)
}

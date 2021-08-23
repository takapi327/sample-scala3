package zenn.json

case class JsonNumber(n: MyJsons.MyJsonNumber) extends Json

object JsonNumber

object MyJsons {
  type MyJsonNumber = MyInt | MyDouble | MyFloat |MyBigDecimal
  opaque type MyInt        = Int
  opaque type MyDouble     = Double
  opaque type MyFloat      = Float
  opaque type MyBigDecimal = BigDecimal
  object MyInt {
    def apply(i:Int):MyInt = i
  }
  extension (i:MyJsonNumber)
    def asJson: Json = JNumber(i)
}

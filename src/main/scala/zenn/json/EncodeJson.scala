package zenn.json

trait EncodeJson[A] {
  def apply(a: A): Json = encode(a)

  def encode(a: A): Json
}

object EncodeJson extends EncodeJsons {
  def apply[A](f: A => Json): EncodeJson[A] = new EncodeJson[A] {
    def encode(a: A): Json = f(a)
  }

  def of[A: EncodeJson]: EncodeJson[A] = summon[EncodeJson[A]]
}

trait EncodeJsons {
  given EncodeJson[String] with
    def encode(a: String) = JString(a)

  //given EncodeJson[Int] with
  //  def encode(a:Int) = JNumber(a.asJsonNumberByExt)

  given EncodeJson[Boolean] with
    def encode(a: Boolean) = JBool(a)

  given EncodeJson[Null] with
    def encode(a: Null) = JNull

  given EncodeJson[Json] with
    def encode(a: Json) = a

  given [T: EncodeJson]: EncodeJson[Option[T]] with
    def encode(a: Option[T]) = a match {
      case Some(v) => summon[EncodeJson[T]].encode(v)
      case None    => JNull
    }
}

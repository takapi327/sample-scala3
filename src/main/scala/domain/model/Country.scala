package domain.model

case class Country(code: String, name: String, pop: Int, gnp: Option[Double])
case class Code(code: String)
case class Country2(name: String, pop: Int, gnp: Option[Double])

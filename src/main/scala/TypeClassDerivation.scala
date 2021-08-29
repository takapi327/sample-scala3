//trait Eq[T]:
//  def eqv(x: T, y: T): Boolean
//
//import scala.deriving.Mirror
//import scala.compiletime.{erasedValue, summonInline, summonAll}
//
//inline given derived[T](using m: Mirror.Of[T]): Eq[T] =
//  val elmInstances = summonAll[m.MirroredElemTypes]
//  inline m match
//    case s: Mirror.SumOf[T]     => eqSum(s, elmInstances)
//    case p: Mirror.ProductOf[T] => eqProduct(p, elmInstances)

class Box[T](x: T) derives CanEqual

object Box
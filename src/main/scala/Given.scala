
import scala.util.NotGiven

trait Ord[T]:
  def compare(x: T, y: T): Int
  extension (x: T) def < (y: T) = compare(x, y) < 0
  extension (x: T) def > (y: T) = compare(x, y) > 0

given intOrd: Ord[Int] with
  def compare(x: Int, y: Int) =
    if x < y then -1 else if x > y then +1 else 0

given listOrd[T](using ord: Ord[T]): Ord[List[T]] with
  // xs: List(1, 2, 3), ys: List(4, 5, 6)
  def compare(xs: List[T], ys: List[T]): Int = (xs, ys) match
    case (Nil, Nil) => 0
    case (Nil, _)   => -1
    case (_, Nil)   => +1
    case (x :: xs1, y :: ys1) =>
      val fst = ord.compare(x, y)
      println("======================")
      println(x)   // 1
      println(y)   // 4
      println(xs1) // List(2, 3)
      println(ys1) // List(5, 6)
      println(fst) // -1
      println("======================")
      if fst != 0 then fst else compare(xs1, ys1)

object Ord extends Ord[Int]:
  def compare(x: Int, y: Int): Int = x + y

object OrdTest:
  def testGiven(x: Int, y: Int)(using ord: Ord[Int]): Int =
    ord.compare(x, y)

  def testGivenList(x: List[Int], y: List[Int])(using ord: Ord[List[Int]]): Int =
    ord.compare(x, y)

trait Tagged[A]

case class TaggedFoo[A](bool: Boolean)
object TaggedFoo:
  given fooTagged[A](using Tagged[A]): TaggedFoo[A] = TaggedFoo(true)
  given fooNotTagged[A](using NotGiven[Tagged[A]]): TaggedFoo[A] = TaggedFoo(false)
  given Tagged[Int]()
end TaggedFoo

def max[T](x: T, y: T)(using ord: Ord[T]): T =
  if ord.compare(x, y) < 0 then y else x

def maximum[T](xs: List[T])(using Ord[T]): T =
  xs.reduceLeft(max) // xs.reduceLeft((x, y) => max(x, y))

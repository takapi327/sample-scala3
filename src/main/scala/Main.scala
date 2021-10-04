
import UnionTypes._
import Enumerations._
import IntersectionTypes.*
import Ord.*
import OrdTest.*
import TaggedFoo.*
import Extension.{ given, * }

import SensorReader.*

import domain.value.Email

@main def Main: Unit =
  println("Hello world!")
  println(msg)
  println(leafElem(Seq(1, 3, 4)))
  println(help(UserName("takapi")))
  println(bar[Int](List(1, 2, 3)))
  println(Color.Red.ordinal)
  println(Color.Green.ordinal)
  println(Color.Blue.ordinal)
  println(Color.valueOf("Blue"))
  println(Color.values)
  println(Color.fromOrdinal(0))
  println(t(0))
  println((num, str, person))
  for { i <- ints } yield println(i)
  for
    i <- ints.reverse
  do
    println(i)
  for
    i <- 1 to 2
    j <- 'a' to 'b'
    k <- 1 to 10 by 5
  do
    println(s"i = $i, j = $j, k = $k")
  test(0)
  test(10)
  val s1 = Sensor("sensor1")
  val s2 = Sensor("sensor2")
  val d1 = Display()
  val d2 = Display()
  s1.subscribe(d1)
  s1.subscribe(d2)
  s2.subscribe(d1)
  s1.changeValue(2)
  s2.changeValue(3)
  val pizza = Pizza(Small, Thin, Seq(Cheese))
    .addTopping(Pepperoni)
    .updateCrustType(Thick)
    .price
  println(pizza)
  println(C().children)
  println(compare(1, 2) > 0)
  println(testGiven(1, 2))
  println(testGivenList(Nil, Nil))
  println(testGivenList(List(1, 2, 3), Nil))
  println(testGivenList(Nil, List(4, 5, 6)))
  println(testGivenList(List(1, 2, 3), List(4, 5, 6)))
  println(summon[TaggedFoo[Int]].bool)
  println(!summon[TaggedFoo[String]].bool)
  println(max(3, 2))
  println(max(List(1, 2, 3), List(4, 5, 6)))
  println(maximum(List(1, 2, 3)))
  println(summon[Ord[List[Int]]])
  println(Circle(0, 0, 1).circumference)
  println(4.safeMod(2))
  println("Takapi".position('p', 2))
  println(combineAll(List(1, 2, 3)))
  println(assertTransformation(List("a", "b"), elt => s"${elt}-1"))
  println(boolTransformation(List("a1", "b1"), List("a", "b"), elt => s"${elt}1"))
  println(monadTest1(List("a", "b"), elt => List(s"${elt}-1")))
  println(monadTest2(List("a", "b"), elt => s"${elt}-1"))
  val optStr: Option[String] = Some("a")
  println(monadTest0[List, String]("A"))
  println(monadTest1(optStr, elt => Some(s"${elt}-1")))
  println(monadTest2(optStr, elt => s"${elt}-1"))

  println(monadTest1(monadTest0[List, String]("A"), elt => List(s"${elt}-1")))
  println(monadTest1(monadTest0[Option, String]("A"), elt => Some(s"${elt}-1")))
  println(Monad[List].pure("A"))
  println(Monad[Option].pure("A"))
  println(Monad[Seq].pure("A"))
  println(new Box(1) == new Box(1))
  println(new Box(1) == new Box(1L))
  println(new Box(1) == new Box("1"))
  println(new Box(Seq(1, 2)) == new Box(Seq(3, 4)))
  println(Email("test@ezweb.ne.jp"))
  println(Email("test@ezweb.ne.jp").matchRegex)
  println(Email("test@ezweb.ne.jp").matchRegex2)
  //println(Email("テスト@ezweb.ne.jp").matchRegex2)
  println(Email("test@ezweb.ne.jp").refine)
  println(Email("テスト@ezweb.ne.jp").refine)

  println(p1 == p2)
  println(pBook == aBook)
  println(pBook == aBook)
  println(s)
  println(s1)
  println(s2)
  println(s3)
  println(s4)

def msg = "I was compiled by Scala 3. :)"

def foo[A](xs: List[A]): List[A] = xs.reverse

val bar: [A] => List[A] => List[A]
  = [A] => (xs: List[A]) => foo[A](xs)

enum Expr[A]:
  case Var(name: String)
  case Apply[A, B](fun: Expr[B => A], arg: Expr[B]) extends Expr[A]

case class Person(name: String)

val t = (11, "eleven", Person("Eleven"))

val (num, str, person) = t

val ints = Seq(1, 2, 3)

val list =
  for
    i <- 10 to 12
  yield
    i * 2

val list2 =
  for
    i <- 10 to 12
  do
    i * 2

def test(i: Int) =
  i match
    case 0    => println("1")
    case 1    => println("2")
    case what => println(s"You gave me: $what" )

trait SubjectObserver:

  type S <: Subject
  type O <: Observer

  trait Subject { self: S =>
    private var observers: List[O] = List()
    def subscribe(obs: O): Unit =
      observers = obs :: observers
    def publish() =
      for obs <- observers do obs.notify(this)
  }

  trait Observer {
    def notify(sub: S): Unit
  }

object SensorReader extends SubjectObserver:
  type S = Sensor
  type O = Display

  class Sensor(val label: String) extends Subject:
    private var currentValue = 0.0
    def value = currentValue
    def changeValue(v: Double) =
      currentValue = v
      publish()

  class Display extends Observer:
    def notify(sub: Sensor) =
      println(s"${sub.label} has value ${sub.value}")

enum CrustSize:
  case Small, Medium, Large

enum CrustType:
  case Thin, Thick, Regular

enum Topping:
  case Cheese, Pepperoni, BlackOlives, GreenOlives, Onions

// the companion object of enumeration Topping
object Topping:
  // the implementation of `toppingPrice` above
  def price(t: Topping): Double = t match
    case Cheese | Onions => 0.5
    case Pepperoni | BlackOlives | GreenOlives => 0.75

import CrustSize.*
import CrustType.*
import Topping.*

case class Pizza(
  crustSize: CrustSize,
  crustType: CrustType,
  toppings:  Seq[Topping]
)

extension (p: Pizza)
  def price: Double =
    pizzaPrice(p) // implementation from above

  def addTopping(t: Topping): Pizza =
    p.copy(toppings = p.toppings :+ t)

  def removeAllToppings: Pizza =
    p.copy(toppings = Seq.empty)

  def updateCrustSize(cs: CrustSize): Pizza =
    p.copy(crustSize = cs)

  def updateCrustType(ct: CrustType): Pizza =
    p.copy(crustType = ct)

object Pizza:
  // the implementation of `pizzaPrice` from above
  def price(p: Pizza): Double = ???


def pizzaPrice(p: Pizza): Double = p match
  case Pizza(crustSize, crustType, toppings) =>
    val base  = 6.00
    val crust = crustPrice(crustSize, crustType)
    val tops  = toppings.map(toppingPrice).sum
    base + crust + tops

def toppingPrice(t: Topping): Double = t match
  case Cheese | Onions => 0.5
  case Pepperoni | BlackOlives | GreenOlives => 0.75

def crustPrice(s: CrustSize, t: CrustType): Double =
  (s, t) match
    case (Small | Medium, _) => 0.25
    case (Large, Thin)       => 0.50
    case (Large, Regular)    => 0.75
    case (Large, Thick)      => 1.00

trait Logarithms:

  opaque type Logarithm = Int

  // operations on Logarithm
  def add(x: Logarithm, y: Logarithm): Logarithm
  def mul(x: Logarithm, y: Logarithm): Logarithm

  // functions to convert between Double and Logarithm
  def make(d: Double): Logarithm
  def extract(x: Logarithm): Double

  // extension methods to use `add` and `mul` as "methods" on Logarithm
  extension (x: Logarithm)
    def toDouble: Double = extract(x)
    def + (y: Logarithm): Logarithm = add(x, y)
    def * (y: Logarithm): Logarithm = mul(x, y)

object LogarithmsImpl extends Logarithms:

  type Logarithm = Double

  // operations on Logarithm
  def add(x: Logarithm, y: Logarithm): Logarithm = make(x.toDouble + y.toDouble)
  def mul(x: Logarithm, y: Logarithm): Logarithm = x + y

  // functions to convert between Double and Logarithm
  def make(d: Double): Logarithm = math.log(d)
  def extract(x: Logarithm): Double = math.exp(x)

import LogarithmsImpl.*
val l: Logarithm = make(1.0)
val d: Double = l

object Logarithms2:
  //vvvvvv this is the important difference!
  opaque type Logarithm2 = Double

  object Logarithm2:
    def apply(d: Double): Logarithm2 = math.log(d)

  extension (x: Logarithm2)
    def toDouble: Double = math.exp(x)
    def + (y: Logarithm2): Logarithm2 = Logarithm2(math.exp(x) + math.exp(y))
    def * (y: Logarithm2): Logarithm2 = x + y

import Logarithms2.*
val l2 = Logarithm2(2.0)
val l3 = Logarithm2(3.0)

val test1: Double = (l2 * l3).toDouble
val test2: Double = (l2 + l3).toDouble
val test3: Logarithm2 = l2 * l3

val d2: Double = test3.toDouble

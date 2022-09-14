
import java.util.concurrent.Executors

import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.concurrent.duration.*

import cats.effect.*
import cats.effect.unsafe.implicits.global
import cats.implicits.*

@main def Main: Unit =

  val hello = MyIO.putStr("hello!")
  val world = MyIO.putStr("world!")

  val helloWorld: MyIO[Unit] =
    for
      _ <- hello
      _ <- world
    yield ()

  helloWorld.unsafeRun()

  val ohNoes: IO[Int] =
    IO(throw new RuntimeException("oh noes!"))

  given scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
  val ohNoesFuture: Future[Int] =
    Future(throw new RuntimeException("oh noes!"))

  println(ohNoesFuture)

object HelloWold extends IOApp:
  def run(args: List[String]): IO[ExitCode] =
    tickingClock.as(ExitCode.Success)

  def tickingClock: IO[Unit] =
    for
      _ <- IO.println(System.currentTimeMillis)
      _ <- IO.sleep(1.second)
      _ <- tickingClock
    yield ()

@main def Future1(): Future[Unit] =
  given ExecutionContext = ExecutionContext.global

  val hello = Future(println(s"[${Thread.currentThread.getName}] Hello"))
  val world = Future(println(s"[${Thread.currentThread.getName}] World"))

  val hw1: Future[Unit] =
    for
      _ <- hello
      _ <- world
    yield ()

  Await.ready(hw1, 5.seconds)

  val hw2: Future[Unit] =
    (hello, world).mapN((_, _) => ())

  Await.ready(hw2, 5.seconds)

@main def Future2(): Future[Unit] =
  given ExecutionContext = ExecutionContext.global

  def hello(num: Int) = Future(println(s"[${Thread.currentThread.getName}] Hello $num"))
  def world(num: Int) = Future(println(s"[${Thread.currentThread.getName}] World $num"))

  val hw1: Future[Unit] =
    for
      _ <- hello(1)
      _ <- world(1)
    yield ()

  Await.ready(hw1, 5.seconds)

  val hw2: Future[Unit] =
    (hello(2), world(2)).mapN((_, _) => ())

  Await.ready(hw2, 5.seconds)

extension [A](ioa: IO[A])
  def debug: IO[A] = for
    a  <- ioa
    tn = Thread.currentThread.getName
    _  = println(s"[$tn] $a")
  yield a

@main def IOComposition: Unit =
  val hello = IO.println(s"[${Thread.currentThread.getName}] Hello")
  val world = IO.println(s"[${Thread.currentThread.getName}] World")

  val hw1: IO[Unit] = for
    _ <- hello
    _ <- world
  yield ()

  val hw2: IO[Unit] =
    (hello, world).mapN((_, _) => ())

  hw1.unsafeRunSync()
  hw2.unsafeRunSync()

object DebugExample extends IOApp:

  def run(args: List[String]): IO[ExitCode] =
    seq.as(ExitCode.Success)

  val hello = IO("hello").debug
  val world = IO("world").debug

  val seq = (hello, world)
    .mapN((h, w) => s"$h $w")
    .debug

object ParMapN extends IOApp:

  def run(args: List[String]): IO[ExitCode] =
    par.as(ExitCode.Success)

  val hello = IO("hello").debug
  val world = IO("world").debug

  val par = (hello, world)
    .parMapN((h, w) => s"$h $w")
    .debug

object ParMapNErrors extends IOApp:
  def run(args: List[String]): IO[ExitCode] =
    e1.attempt.debug *>
      e2.attempt.debug *>
      e3.attempt.debug *>
      IO.pure(ExitCode.Success)

  val ok  = IO("hi").debug
  val ko1 = IO.sleep(1.second).as("ko1").debug *>
    IO.raiseError[String](new RuntimeException("oh!")).debug
  val ko2 = IO.raiseError[String](RuntimeException("noes!")).debug
  val e1  = (ok, ko1).parTupled.void
  val e2  = (ko1, ok).parTupled.void
  val e3  = (ko1, ko2).parTupled.void

object ParTraverse extends IOApp:
  def run(args: List[String]): IO[ExitCode] =
    tasks.parTraverse(task).debug.as(ExitCode.Success)

  val numTasks = 100
  val tasks: List[Int] = List.range(0, numTasks)
  def task(id: Int): IO[Int] = IO(id).debug

object Test:
  given String = "test"
  given Long   = 1L
  def test(t: Long ?=> String ?=> Unit): Unit = t

@main def contextFunctionTest =

  Test.test {
    println(summon[String])
    println(summon[Long])
  }



/*
def get(using Int): String = summon[Int].toString

def pf1: PartialFunction[String, Int => String] = {
  case "hoge" => int => int.toString
  case "huga" => int => int.toString
}

def pf2: PartialFunction[String, Int ?=> String] = {
  case "hoge" => get
  case "huga" => get
}

println(pf1.isDefinedAt("hoge"))
println(pf1.isDefinedAt("huga"))
println(pf1("huga")(1))
println(pf1.unapply("hogehoge"))
println(pf1.isDefinedAt("hogehoge"))

println(pf2.isDefinedAt("hoge"))
println(pf2.isDefinedAt("huga"))
println(pf2("huga")(using 2))
println(pf2.unapply("hogehoge"))
println(pf2.isDefinedAt("hogehoge"))
*/

  //def lift1 = new LiftedTest(pfTest1)
  //println(lift1("hoge"))
  //println(lift1("huga"))
  //println(lift1("hogehoge"))

  //def lift2 = new LiftedTest(pfTest2)
  //println(lift2("hoge"))
  //println(lift2("huga"))
  //println(lift2("hogehoge"))

  //lazy val fallback_fn: Any => Any = _ => fallback_fn
  //def checkFallback[B] = fallback_fn.asInstanceOf[Any => B]
  //def fallbackOccurred[B](x: B) = fallback_fn eq x.asInstanceOf[AnyRef]
//
  //class LiftedTest[-A, +B] (val pf: PartialFunction[A, B])
  //  extends scala.runtime.AbstractFunction1[A, Option[B]] with Serializable {
//
  //  def apply(x: A): Option[B] = {
  //    val z = pf.applyOrElse(x, checkFallback[B])
  //    if (!fallbackOccurred(z)) Some(z) else None
  //  }
  //}

case class MyIO[A](unsafeRun: () => A):
  def map[B](f: A => B): MyIO[B] =
    MyIO(() => f(unsafeRun()))

  def flatMap[B](f: A => MyIO[B]): MyIO[B] =
    MyIO(() => f(unsafeRun()).unsafeRun())

object MyIO:
  def putStr(s: => String): MyIO[Unit] =
    MyIO(() => println(s))

/*
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
*/
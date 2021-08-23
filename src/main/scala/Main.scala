import UnionTypes._
import Enumerations._

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
  println(Enumerations.Option.Some("Hello"))
  println(Enumerations.Option.None)
  println(Enumerations.Option.None.isDefined)
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

  //for
  //  i <- 1 to 10
  //  if i > 3
  //  if i < 6
  //  if i % 2 == 0
  //do
  //  println(i)

  //println(list)
  //println(list2)
  test(0)
  test(10)

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
/*
def mapSubexpressions[A](e: Expr[A])(f: [B] => Expr[B] => Expr[B]): Expr[A] =
  e match
    case Apply(fun, arg) => Apply(f(fun), f(arg))
    case Var(n)          => Var(n)

val e0 = Apply(Var("f"), Var("a"))
val e1 = mapSubexpressions(e0)(
  [B] => (se: Expr[B]) => Apply(Var[B => B]("wrap"), se)
)
*/
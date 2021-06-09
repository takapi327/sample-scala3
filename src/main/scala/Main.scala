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

def msg = "I was compiled by Scala 3. :)"

def foo[A](xs: List[A]): List[A] = xs.reverse

val bar: [A] => List[A] => List[A]
  = [A] => (xs: List[A]) => foo[A](xs)

enum Expr[A]:
  case Var(name: String)
  case Apply[A, B](fun: Expr[B => A], arg: Expr[B]) extends Expr[A]

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
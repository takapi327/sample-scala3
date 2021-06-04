import UnionTypes._

@main def Main: Unit =
  println("Hello world!")
  println(msg)
  println(leafElem(Seq(1, 3, 4)))
  println(help(UserName("takapi")))
  println(bar[Int](List(1, 2, 3)))

def msg = "I was compiled by Scala 3. :)"

def foo[A](xs: List[A]): List[A] = xs.reverse

val bar: [A] => List[A] => List[A]
  = [A] => (xs: List[A]) => foo[A](xs)
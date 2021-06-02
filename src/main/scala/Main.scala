import UnionTypes._

@main def Main: Unit =
  println("Hello world!")
  println(msg)
  println(leafElem(Seq(1, 3, 4)))
  println(help(UserName("takapi")))

def msg = "I was compiled by Scala 3. :)"

@main def Main: Unit =
  println("Hello world!")
  println(msg)
  println(UnionTypes.leafElem(Seq(1, 3, 4)))

def msg = "I was compiled by Scala 3. :)"

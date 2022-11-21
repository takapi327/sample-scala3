package `macro`

object Test extends App:
  Debug.hello()

  val z = 2

  def test =
    val x = 0
    val y = 1

    println("--")

    Debug.debugSingle(x)
    Debug.debugSingle(x + y)

    println("--")

    Debug.debug(x)
    Debug.debug(x, y)
    Debug.debug(x + y)
    Debug.debug(x, x + y)
    Debug.debug("A", x, x + y)
    Debug.debug("A", x, "B", y)
    Debug.debug(x, y, z)

  test

  println("--")

  PrintTree.printTree {
    (s: String) => s.length
  }

  trait Base
  case class Test(name: String, age: Long) extends Base
  println(PrintTree.name[Test])
  println(PrintTree.fieldMembers[Test])
  println(PrintTree.caseFields[Test])
  println(PrintTree.children[Test])

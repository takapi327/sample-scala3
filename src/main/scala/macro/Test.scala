package `macro`

import scala.quoted.*

abstract class Inject:
  def print(): String

  override def toString: String = print()

case class InjectImpl() extends Inject :
  //def this() = this("Hello Impl")
  override def print(): String = "Hello Impl"

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

  //PrintTree.printTree {
  //  (s: String) => s.length
  //}

  //trait Base
  //case class Test(name: String, age: Long) extends Base
  //println(PrintTree.name[Test])
  //println(PrintTree.fieldMembers[Test])
  //println(PrintTree.caseFields[Test])
  //println(PrintTree.children[Test])

  println("--")

  val hoge: Inject = Debug.inject[Inject]("macro.InjectSymbol")
  class Hoge(val base: Inject)

  println(hoge.print())
  println("--")

  def constructModule[T](className: String, args: AnyRef*): T =
    val argTypes = args.map(_.getClass)
    val cls = ClassLoader.getSystemClassLoader.loadClass(className)
    val constructor = cls.getConstructor(argTypes: _*)
    constructor.setAccessible(true)
    constructor.newInstance(args: _*).asInstanceOf[T]

  val t = constructModule[Inject]("macro.InjectImpl")
  println(t.print())

package intersectionTypes

trait A:
  def children: List[Int]

trait B:
  def children: List[Int]

val x: A & B = new C
val y: List[Int & Int] = x.children

class C extends A, B:
  def children: List[Int & Int] = List(1, 2, 3)

case class Circle(
  x:      Double,
  y:      Double,
  radius: Double
)

extension (c: Circle)
  def circumference: Double = c.radius * math.Pi * 2

trait IntOps:
  extension (i: Int) def isZero: Boolean = i == 0

  extension (i: Int) def safeMod(x: Int): Option[Int] =
  // extension method defined in same scope IntOps
    if x.isZero then None
    else Some(i % x)

object IntOpsEx extends IntOps:
  extension (i: Int) def safeDiv(x: Int): Option[Int] =
  // extension method brought into scope via inheritance from IntOps
    if x.isZero then None
    else Some(i / x)

trait SafeDiv:
  import IntOpsEx.* // brings safeDiv and safeMod into scope

  extension (i: Int) def divide(d: Int): Option[(Int, Int)] =
  // extension methods imported and thus in scope
    (i.safeDiv(d), i.safeMod(d)) match
      case (Some(d), Some(r)) => Some((d, r))
      case _ => None

object Extension:
  given intOps: IntOps()
  extension (s: String)
    def position(ch: Char, n: Int): Int =
      if n < s.length && s(n) != ch then position(ch, n + 1)
      else n
  
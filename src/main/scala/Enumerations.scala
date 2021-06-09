object Enumerations:

  enum Color(val rgb: Int):
    case Red   extends Color(0xFF0000)
    case Green extends Color(0x00FF00)
    case Blue  extends Color(0x0000FF)

  enum Option[+T]:
    case Some(x: T)
    case None

    def isDefined: Boolean = this match
      case None => false
      case _    => true

  object Option:

    def apply[T >: Null](x: T): Option[T] =
      if x == null then None else Some(x)

  end Option
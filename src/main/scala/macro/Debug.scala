package `macro`

import scala.quoted.*

object Debug:
  inline def hello(): Unit = println("Hello, world!")

  inline def debugSingle(inline expr: Any): Unit = ${debugSingleImpl('expr)}
  def debugSingleImpl(expr: Expr[Any])(using Quotes): Expr[Unit] =
    '{ println("Value of " + ${Expr(expr.show)} + " is " + $expr) }

  inline def debug(inline exprs: Any*): Unit = ${debugImpl('exprs)}
  def debugImpl(exprs: Expr[Seq[Any]])(using q: Quotes): Expr[Unit] =

    import q.reflect.*

    def showWithValue(e: Expr[?]): Expr[String] =
      '{${Expr(e.show)} + " = " + $e}

    val stringExps: Seq[Expr[String]] = exprs match
      case Varargs(es) =>
        es.map { e =>
          e.asTerm match
            case Literal(c: Constant) => Expr(c.value.toString)
            case _ => showWithValue(e)
        }
      case e => List(showWithValue(e))

    val concatenatedStringsExp = stringExps.reduceOption((e1, e2) => '{$e1 + ", " + $e2})
      .getOrElse('{""})

    '{println($concatenatedStringsExp)}

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

  import scala.deriving.Mirror
  inline def mirror[T](using m: Mirror.Of[T]): m.MirroredElemTypes = ???

  inline def inject[T](inline className: String): T = ${injectImpl[T]('className)}
  @annotation.experimental
  def injectImpl[T: Type](className: Expr[String])(using q: Quotes): Expr[T] =
    import q.reflect.*
    val parents = List(TypeTree.of[T])
    def decls(cls: Symbol): List[Symbol] =
      List(Symbol.newMethod(cls, "print", MethodType(Nil)(_ => Nil, _ => TypeRepr.of[String])))
    val cls = Symbol.newClass(Symbol.spliceOwner, className.show, parents = parents.map(_.tpe), decls, selfType = None)
    //val cls = Symbol.classSymbol(className.show)
    val printSym = cls.declaredMethod("print").head
    val printDef = DefDef(printSym, _ => Some('{"Hello"}.asTerm))
    val clsDef = ClassDef(cls, parents, body = List(printDef))
    val newCls = Typed(Apply(Select(New(TypeIdent(cls)), cls.primaryConstructor), Nil), TypeTree.of[T])
    val list = List(clsDef)
    Block(list, newCls).asExprOf[T]

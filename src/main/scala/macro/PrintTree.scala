package `macro`

import scala.quoted.*

object PrintTree:
  inline def printTree[T](inline x: T): Unit = ${printTreeImpl('x)}
  def printTreeImpl[T: Type](x: Expr[T])(using qctx: Quotes): Expr[Unit] =
    import qctx.reflect.*
    println(x.asTerm.show(using Printer.TreeStructure))
    '{()}

  inline def name[T]: String = ${nameImpl[T]}
  def nameImpl[T: Type](using Quotes): Expr[String] =
    import quotes.reflect.*
    Expr(TypeRepr.of[T].typeSymbol.name)

  inline def fieldMembers[T]: String = ${fieldMembersImpl[T]}
  def fieldMembersImpl[T: Type](using Quotes): Expr[String] =
    import quotes.reflect.*
    Expr(TypeRepr.of[T].typeSymbol.fieldMembers.mkString(", "))

  inline def caseFields[T]: String = ${caseFieldsImpl[T]}
  def caseFieldsImpl[T: Type](using Quotes): Expr[String] =
    import quotes.reflect.*
    Expr(TypeRepr.of[T].typeSymbol.caseFields.mkString(", "))

  inline def children[T]: String = ${childrenImpl[T]}
  def childrenImpl[T: Type](using Quotes): Expr[String] =
    import quotes.reflect.*
    Expr(TypeRepr.of[T].typeSymbol.children.mkString(","))

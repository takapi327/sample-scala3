
import cats._, data._

import org.atnos.eff._
import org.atnos.eff.all._
import org.atnos.eff.syntax.all._

type ReaderInt[A]    = Reader[Int, A]
type WriterString[A] = Writer[String, A]

type Stack = Fx.fx3[WriterString, ReaderInt, Eval]

type _readerInt[R]    = ReaderInt |= R
type _writerString[R] = WriterString |= R

def program[R :_readerInt :_writerString :_eval]: Eff[R, Int] =
  for 
    n <- ask[R, Int]
    _ <- tell("必要な乗数は" + n)
    a <- delay(math.pow(2, n.toDouble).toInt)
    _ <- tell("結果は" + a)
  yield
    a
    
@main def EffMain: Unit =
  println(program[Stack].runReader(6).runWriter.runEval.run)
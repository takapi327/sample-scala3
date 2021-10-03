import scala.concurrent.ExecutionContext

type Executable[T] = ExecutionContext ?=> T

object PostConditions:
  opaque type WrappedResult[T] = T

  def result[T](using r: WrappedResult[T]): T = r

  extension [T](x: T)
    def ensuring(condition: WrappedResult[T] ?=> Boolean): T = {
      //assert(condition(using x))
      println(s"-----${x}-----------")
      println(condition(using x))
      x
    }
end PostConditions

import PostConditions.{ensuring, result}

val int = List(1, 2, 3).sum
val s   = List(1, 2, 3).sum.ensuring(result == 6)
val s1  = "hogehoge".ensuring(result == "hogehoge")
val s2  = 1.ensuring(result == 1)
val s3  = List(1, 2, 3).ensuring(result == List(3, 2, 3))
val s4  = int.ensuring(result == 6)

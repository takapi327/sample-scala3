/*
import scala.collection.mutable._

import cats.*
import cats.data.*
import cats.implicits.*

import org.atnos.eff.*
import org.atnos.eff.all.*
import org.atnos.eff.syntax.all.*
import org.atnos.eff.interpret.*

sealed trait KVStore[+A]

case class Put[T](key: String, value: T) extends KVStore[Unit]
case class Get[T](key: String) extends KVStore[Option[T]]
case class Delete(key: String) extends KVStore[Unit]

type _kvstore[R] = KVStore |= R

def store[T, R: _kvstore](key: String, value: T): Eff[R, Unit] =
  Eff.send[KVStore, R, Unit](Put(key, value))

def find[T, R: _kvstore](key: String): Eff[R, Option[T]] =
  Eff.send[KVStore, R, Option[T]](Get(key))

def delete[T, R: _kvstore](key: String): Eff[R, Unit] =
  Eff.send(Delete(key))

def update[T, R: _kvstore](key: String, f: T => T): Eff[R, Unit] =
  for
    ot <- find[T, R](key)
    _  <- ot.map(t => store[T, R](key, f(t))).getOrElse(Eff.pure(()))
  yield
    ()

def program[R: _kvstore]: Eff[R, Option[Int]] =
  for
    _ <- store("wild-cats", 2)
    _ <- update[Int, R]("wild-cats", _ + 12)
    _ <- store("tame-cats", 5)
    n <- find[Int, R]("wild-cats")
    _ <- delete("tame-cats")
  yield
    n

/*
def runKVStoreUnsafe[R, A](effects: Eff[R, A])(using m: KVStore <= R): Eff[m.Out, A] =
  val kvs = Map.empty[String, Any]

  val sideEffect = new SideEffect[KVStore] {
    def apply[X](kv: KVStore[X]): X =
      kv match
        case Put(key, value) =>
          println(s"put($key, $value)")
          kvs.put(key, value)
          ().asInstanceOf[X]
        case Get(key) =>
          println(s"get($key)")
          kvs.get(key).asInstanceOf[X]
        case Delete(key) =>
          println(s"delete($key)")
          kvs.remove(key)
          ().asInstanceOf[X]
    
    def applicative[X, Tr[_]: Traverse](ms: Tr[KVStore[X]]): Tr[X] =
      ms.map(apply)
  }
  interpretUnsafe(effects)(sideEffect)(m)
 */

type _writerString[R] = Writer[String, *] |= R
type _stateMap[R]     = State[Map[String, Any], *] |= R

def runKVStore[R, U, A](effects: Eff[R, A])(using
  member:    Member.Aux[KVStore, R, U],
  throwable: _throwableEither[U],
  writer:    _writerString[U],
  state:     _stateMap[U]
): Eff[U, A] =
  translate(effects)(new Translate[KVStore, U] {
    def apply[X](kv: KVStore[X]): Eff[U, X] =
      kv match
        case Put(key, value) =>
          for
            _ <- tell(s"put($key, $value)")
            //_ <- modify((map: Map[String, Any]) => map.clone().addOne((key, value)))
            _ <- modify((map: Map[String, Any]) => map += (key -> value))
            r <- fromEither(Either.catchNonFatal(().asInstanceOf[X]))
          yield r
        case Get(key) =>
          for
            _ <- tell(s"get($key)")
            m <- get[U, Map[String, Any]]
            r <- fromEither(Either.catchNonFatal(m.get(key).asInstanceOf[X]))
          yield r
        case Delete(key) =>
          for
            _ <- tell(s"delete($key)")
            _ <- modify((map: Map[String, Any]) => map -= key)
            r <- fromEither(Either.catchNonFatal(().asInstanceOf[X]))
          yield r
  })

extension [R, U, A](effects: Eff[R, A])(using
  member:    Member.Aux[KVStore, R, U],
  throwable: _throwableEither[U],
  writer:    _writerString[U],
  state:     _stateMap[U]
)
  def runStore = runKVStore(effects)


sealed trait Maybe[A]
case class Just[A](a: A) extends Maybe[A]
case class Nothing[A]() extends Maybe[A]

object MaybeEffect:
  type _maybe[R] = Maybe |= R

  def just[R: _maybe, A](a: A): Eff[R, A] = Eff.send[Maybe, R, A](Just(a))
  def nothing[R: _maybe, A]():  Eff[R, A] = Eff.send[Maybe, R, A](Nothing())
  
  def runMaybe[R, U, A, B](effect: Eff[R, A])(
    using m: Member.Aux[Maybe, R, U]
  ): Eff[U, Option[A]] =
    recurse(effect)(new Recurser[Maybe, U, A, Option[A]]:
      def onPure(a: A): Option[A] = Some(a)
      
      def onEffect[X](m: Maybe[X]): Either[X, Eff[U, Option[A]]] =
        m match
          case Just(x)   => Left(x)
          case Nothing() => Right(Eff.pure(None))

      def onApplicative[X, T[_]: Traverse](ms: T[Maybe[X]]): Either[T[X], Maybe[T[X]]] =
        Right(ms.sequence)
    )

  given applicativeMaybe: Applicative[Maybe] = new Applicative[Maybe]:
    def pure[A](a: A): Maybe[A] = Just(a)

    def ap[A, B](ff: Maybe[A => B])(fa: Maybe[A]): Maybe[B] =
      (fa, ff) match
        case (Just(a), Just(f)) => Just(f(a))
        case _                  => Nothing()
        
import MaybeEffect.*

@main def EffMain: Unit =

  type Stack = Fx.fx4[KVStore, Either[Throwable, *], State[Map[String, Any], *], Writer[String, *]]
  val (result, logs) =
    program[Stack].runStore.runEither.evalState(Map.empty[String, Any]).runWriter.run

  println((result.toString +: logs).mkString("\n"))

  val action: Eff[Fx.fx1[Maybe], Int] =
    for
      a <- just(2)
      b <- just(3)
    yield a + b

  println(run(runMaybe(action)))
 */
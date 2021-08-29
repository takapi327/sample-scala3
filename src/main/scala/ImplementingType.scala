trait SemiGroup[T]:
  extension (x: T) def combine (y: T): T

trait Monoid[T] extends SemiGroup[T]:
  def unit: T

given Monoid[String] with
  extension (x: String) def combine (y: String): String = x.concat(y)
  def unit: String = ""

given Monoid[Int] with
  extension (x: Int) def combine (y: Int): Int = x + y
  def unit: Int = 0

object Monoid:
  def apply[T](using m: Monoid[T]) = m

def combineAll[T: Monoid](xs: List[T]): T =
  xs.foldLeft(Monoid[T].unit)(_.combine(_))

trait Functor[F[_]]:
  extension [A](x: F[A])
    def map2[B](f: A => B): F[B]

given Functor[List] with
  extension [A](xs: List[A])
    def map2[B](f: A => B): List[B] =
      xs.map(f)

def assertTransformation[F[_]: Functor, A, B](original: F[A], mapping: A => B): F[B] =
  original.map2(mapping)

def boolTransformation[F[_]: Functor, A, B](expected: F[B], original: F[A], mapping: A => B): Boolean =
  expected == original.map2(mapping)

trait Monad[F[_]] extends Functor[F]:
  def pure[A](x: A): F[A]

  extension [A](x: F[A])
    def flatMap[B](f: A => F[B]): F[B]
    def map[B](f: A => B) = x.flatMap(f.andThen(pure))

end Monad

object Monad:
  def apply[F[_]](using m: Monad[F]) = m

given listMonad: Monad[List] with
  def pure[A](x: A): List[A] = List(x)

  extension [A](xs: List[A])
    def flatMap[B](f: A => List[B]): List[B] =
      xs.flatMap(f)

    def map2[B](f: A => B): List[B] =
      xs.map(f)

given optionMonad: Monad[Option] with
  def pure[A](x: A): Option[A] = Option(x)

  extension [A](xo: Option[A])
    def flatMap[B](f: A => Option[B]): Option[B] =
      xo match {
        case Some(x) => f(x)
        case None    => None
      }

    def map2[B](f: A => B): Option[B] =
      xo.map(f)

given seqMonad: Monad[Seq] with
  def pure[A](x: A): Seq[A] = Seq(x)

  extension [A](xs: Seq[A])
    def flatMap[B](f: A => Seq[B]): Seq[B] =
      xs.flatMap(f)

    def map2[B](f: A => B): Seq[B] =
      xs.map(f)

def monadTest0[F[_]: Monad, A](a: A): F[A] =
  Monad[F].pure(a)

def monadTest1[F[_]: Monad, A, B](arg: F[A], f: A => F[B]): F[B] =
  arg.flatMap(f)

def monadTest2[F[_]: Monad, A, B](arg: F[A], f: A => B): F[B] =
  arg.map2(f)

trait Config
def compute(i: Int)(config: Config): String = ???
def show(str: String)(config: Config): Unit = ???

def computeAndShow(i: Int): Config => Unit = compute(i).flatMap(show)

type ConfigDependent = [Result] =>> Config => Result

given readerMonad[CTX]: Monad[[X] =>> CTX => X] with

  def pure[A](x: A): CTX => A =
    config => x

  extension [A](x: CTX => A)
    def flatMap[B](f: A => CTX => B): CTX => B =
      config => f(x(config))(config)

    def map2[B](f: A => B): CTX => B = ???

end readerMonad

package library.rdb

import cats.effect.*
import com.google.inject.*
import library.util.Configuration

import scala.concurrent.ExecutionContext

trait BaseRepository:

  private lazy val config        = Configuration()
  private lazy val libraryModule = config.get[String]("library.rdb.module")

  private val classType: Class[_] = Class.forName(libraryModule)
  private val constructor = classType.getDeclaredConstructor()
  private val instance    = constructor.newInstance()

  private def createClient[T <: RepositoryClient](module: AbstractModule, classT: Class[T]): RepositoryClient =
    val injector: Injector = Guice.createInjector(module)
    injector.getInstance(classT)

  val service: RepositoryClient = instance match
    case doobie: DoobieModule => createClient(doobie, classOf[RepositoryClient])
    case _ => throw new IllegalArgumentException

class DoobieModule extends AbstractModule:
  override def configure(): Unit =
    bind(classOf[RepositoryClient]).to(classOf[DoobieRepositoryImpl[IO]])

import doobie.*
import doobie.hikari.HikariTransactor

import cats.effect.Resource

trait RepositoryClient:

  type M[_] <: Async

  val ConnectDB: Resource[M, HikariTransactor[M]]

import doobie.implicits.*

import cats.effect.IO.asyncForIO
import cats.effect.unsafe.implicits.global
class Test extends BaseRepository:
  def filterByName(pat: String): List[(String, String)] =
    service.ConnectDB().use {
      sql"select name, code from country where name like $pat"
        .query[(String, String)]
        .to[List]
        .transact[IO]
    }.unsafeRunSync()

class DoobieRepositoryImpl[M[_]: Async](
  using ec: ExecutionContext
) extends DoobieRepository[M], RepositoryClient:

  val ConnectDB(): Resource[M, HikariTransactor[M]] = transactor

/*
import doobie.implicits.*
import org.atnos.eff.*
import org.atnos.eff.addon.doobie.DoobieConnectionIOEffect.*
import org.atnos.eff.interpret.*

import cats.implicits.*
import cats.effect.unsafe.implicits.global

sealed trait RepositoryOp[A]

final case class UseDoobieRepository[T](
  connection: doobie.free.connection.ConnectionIO[T]
) extends RepositoryOp[T]

trait RepositoryCreator:
  //type ThrowableEither[T] = Either[Throwable, T]

  type _repositoryOp[R]    = RepositoryOp |= R
  //type _throwableEither[R] = ThrowableEither |= R

  def runDoobie[R: _repositoryOp, T](
    connection: doobie.free.connection.ConnectionIO[T]
  ): Eff[R, T] =
    EffCreation.send(UseDoobieRepository[T](connection))

object RepositoryCreator extends RepositoryCreator

trait RepositoryInterpretation
  extends DoobieRepository[IO]:

  private def runEffect[R: _connectionIO](connection: doobie.free.connection.ConnectionIO[_]) =
    transactor.use { connection.transact[IO] }.unsafeRunSync()

  def runRepository[R, U, A](effect: Eff[R, A])(
    using member: Member.Aux[RepositoryOp, R, U],
    connectionIO: _connectionIO[U]
  ): Eff[R, A] =
    translate(effect)(new Translate[RepositoryOp, U] {
      override def apply[X](kv: RepositoryOp[X]): Eff[U, X] =
        kv match
          case UseDoobieRepository(connection) => runEffect[U](connection)
    })

object RepositoryInterpretation extends RepositoryInterpretation
*/
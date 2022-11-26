package guice.module

import scala.concurrent.duration.DurationInt

import cats.implicits.*

import cats.effect.*
import cats.effect.implicits.*

import doobie.*
import doobie.implicits.*
import doobie.hikari.HikariTransactor

import guice.model.Connection

trait DatabaseModule extends ResourceModule[Connection]:

  val driver:   String
  val jdbcURL:  String
  val user:     String
  val password: String

  val threadPoolSize: Int = 32

  override val resource: Resource[IO, Connection] =
    (for
      ec <- ExecutionContexts.fixedThreadPool[IO](threadPoolSize)
      xa <- HikariTransactor.newHikariTransactor[IO](
              driver,
              jdbcURL,
              user,
              password,
              ec
            )
    yield new Connection(xa)).evalTap(v => testConnection(v.xa))

  /** A method that tests the initialized database connection and attempts a wait connection at 5 second intervals until
    * a connection is available.
    *
    * @param xa
    *   A thin wrapper around a source of database connections, an interpreter, and a strategy for running programs,
    *   parameterized over a target monad M and an arbitrary wrapped value A. Given a stream or program in ConnectionIO
    *   or a program in Kleisli, a Transactor can discharge the doobie machinery and yield an effectful stream or
    *   program in M.
    */
  def testConnection(xa: Transactor[IO]): IO[Unit] =
    (testQuery(xa) >> IO.println(s"Database connection test complete")/*logger.info(s"$dataSource Database connection test complete")*/).onError { (ex: Throwable) =>
      /*logger.warn(s"$dataSource Database not available, waiting 5 seconds to retry...", ex)*/IO.println("Database not available, waiting 5 seconds to retry...") >>
        IO.sleep(5.seconds) >>  testConnection(xa)
    }

  /** A query to be executed to check the connection to the database.
    *
    * @param xa
    *   A thin wrapper around a source of database connections, an interpreter, and a strategy for running programs,
    *   parameterized over a target monad M and an arbitrary wrapped value A. Given a stream or program in ConnectionIO
    *   or a program in Kleisli, a Transactor can discharge the doobie machinery and yield an effectful stream or
    *   program in M.
    */
  def testQuery(xa: Transactor[IO]): IO[Unit] =
    Sync[IO].void(sql"select 1".query[Int].unique.transact(xa))

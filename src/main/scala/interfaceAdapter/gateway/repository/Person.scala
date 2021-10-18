package interfaceAdapter.gateway.repository

import scala.concurrent.ExecutionContext

import doobie.*
import doobie.implicits.*

import cats.data.*
import cats.effect.*
import cats.implicits.*
import cats.effect.unsafe.implicits.global

import library.rdb.DoobieRepository

import domain.model.*

class PersonRepository(
  using ec: ExecutionContext
) extends DoobieRepository[IO]:

  type PersonInfo = (String, Option[Short])

  def insertMany(ps: List[PersonInfo]): ConnectionIO[Int] =
    val sql = "insert into person (name, age) values (?, ?)"
    Update[PersonInfo](sql).updateMany(ps)

  def byName(pat: String): Either[Throwable, List[(String, String)]] =
    /*
    val fragment:   doobie.util.fragment.Fragment                                                                  = sql"select name, code from country where name like $pat"
    val query:      doobie.util.query.Query0[(String, String)]                                                     = fragment.query[(String, String)]
    val connection: doobie.free.connection.ConnectionIO[List[(String, String)]]                                    = query.to[List]
    val free:       cats.free.Free[doobie.free.connection.ConnectionOp, Either[Throwable, List[(String, String)]]] = connection.attempt
    val transactor: doobie.util.transactor.Transactor[cats.effect.IO] => cats.effect.IO[Either[Throwable, List[(String, String)]]] = free.transact[IO]
     */

    transactor.use {
      sql"select name, code from country where name like $pat" // oobie.util.fragment.Fragment
        .query[(String, String)]                               // doobie.util.query.Query0[(String, String)]
        .to[List]                                              // doobie.free.connection.ConnectionIO[List[(String, String)]]
        .attempt                                               // cats.free.Free[doobie.free.connection.ConnectionOp, Either[Throwable, List[(String, String)]]]
        .transact[IO]                                          // doobie.util.transactor.Transactor[cats.effect.IO] => cats.effect.IO[Either[Throwable, List[(String, String)]]]
    }.unsafeRunSync()

  def byNameToFuture(pat: String): List[(String, String)] =
    transactor.use {
      sql"select name, code from country where name like $pat"
        .query[(String, String)]
        .to[List]
        .transact[IO]
    }.unsafeRunSync()

  def filterByNameToFuture(pat: String): List[(String, String)] =
    transactor.use {
      sql"select name, code from country where name like $pat"
        .query[(String, String)]
        .to[List]
        .transact[IO]
    }.unsafeRunSync()

  def byName2(pat: String): Either[Throwable, List[(String, String)]] =
    transactor.use {
      val query1 = sql"select name, code from country where name like $pat"
        .query[(String, String)]
        .to[List]

      val query2 = sql"select name, code from country where name like $pat"
        .query[(String, String)]
        .to[List]
      val result = for
        t1 <- query1
        t2 <- query2
      yield
        t1 ++ t2

      result.attempt.transact[IO]
    }.unsafeRunSync()

  def query1(pat: String) =
    sql"select name, code from country where name like $pat"
      .query[(String, String)]
      .to[List]

  def query2(pat: String) =
    sql"select name, code from country where name like $pat"
      .query[(String, String)]
      .to[List]

  def run(): Either[Throwable, List[(String, String)]] =
    transactor.use {
      val result = for
        t1 <- query1("N%")
        t2 <- query2("N%")
      yield
        t1 ++ t2
      result.attempt.transact[IO]
    }.unsafeRunSync()
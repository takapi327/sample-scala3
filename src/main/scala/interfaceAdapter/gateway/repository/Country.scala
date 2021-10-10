package interfaceAdapter.gateway.repository

import scala.concurrent.ExecutionContext

import doobie.*
import doobie.implicits.*

import cats.data.*
import cats.effect.*
import cats.implicits.*

import fs2.Stream

import library.rdb.DoobieRepository

import domain.model.*

class CountryRepository (
  using ec: ExecutionContext
) extends DoobieRepository:

  val program1 = 42.pure[ConnectionIO]
  val program2 = sql"select 42".query[Int].unique
  val program3: ConnectionIO[(Int, Double)] =
  for
    a <- sql"select 42".query[Int].unique
    b <- sql"select 52".query[Int].unique
  yield (a, b)
  val program4 = sql"select name from country".query[String].to[List]
  val program5 = sql"select name from country".query[String].stream.take(5).compile.toList
  val program6 = sql"select name from country".query[String].stream.take(5)
  val program7 = sql"select code, name, population, gnp from country".query[(String, String, Int, Option[Double])].stream.take(5)
  val program8 = sql"select code, name, population, gnp from country".query[Country].stream.take(5)
  val program9 = sql"select code, name, population, gnp from country".query[(Code, Country2)].stream.take(5)
  val program11 = sql"select id, name, age from person".query[Person]
  val program12 = sql"update person set age = 15 where name = 'Alice'".update

  def biggerThan(minPop: Int) =
    sql"""
      select code, name, population, gnp
      from country
      where population > $minPop
    """.query[Country]

  def populationIn(range: Range) =
    sql"""
      select code, name, population, gnp
      from country
      where population > ${range.min}
      and   population < ${range.max}
    """.query[Country]

  def populationIn(range: Range, codes: NonEmptyList[String]) =
    val q = fr"""
      select code, name, population, gnp
      from country
      where population > ${range.min}
      and   population < ${range.max}
      and   """ ++ Fragments.in(fr"code", codes) // code IN (...)
    q.query[Country]

  def biggerThan2(minPop: Short) =
    sql"""
      select code, name, population, gnp, indepyear
      from country
      where population > $minPop
    """.query[Country]

  def byName(pat: String) =
    transactor.use {
      sql"select name, code from country where name like $pat"
        .query[(String, String)]
        .to[List]
        .transact[IO]
    }

  def byName2(pat: String) =
    transactor.use {
      sql"select name, code from country where name like $pat"
        .query[(String, String)] // handler will be picked up here
        .to[List]
        .transact[IO]
    }

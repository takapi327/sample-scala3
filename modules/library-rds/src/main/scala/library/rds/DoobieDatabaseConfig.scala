package library.rds

import doobie.*
import doobie.implicits.*
import doobie.util.ExecutionContexts

import cats.*
import cats.data.*
import cats.effect.*
import cats.implicits.*

import fs2.Stream

import scala.concurrent.ExecutionContext

import cats.effect.unsafe.implicits.global

val xa = Transactor.fromDriverManager[IO](
  "com.mysql.cj.jdbc.Driver",
  "jdbc:mysql://127.0.0.1:13306/sample_doobie?useSSL=false",
  "takapi327",
  "takapi327"
)
val y = xa.yolo

case class Country(code: String, name: String, pop: Int, gnp: Option[Double])
case class Code(code: String)
case class Country2(name: String, pop: Int, gnp: Option[Double])

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
val program10: Stream[IO, Country2] =
  sql"select name, population, gnp from country"
    .query[Country2] // Query0[Country2]
    .stream          // Stream[ConnectionIO, Country2]
    .transact(xa)    // Stream[IO, Country2]

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

def biggerThan(minPop: Short) =
  sql"""
    select code, name, population, gnp, indepyear
    from country
    where population > $minPop
  """.query[Country]
  
val io  = program1.transact(xa)
val io2 = program2.transact(xa)
val io3 = program3.transact(xa)
val io4 = program4.transact(xa)
val io5 = program5.transact(xa)

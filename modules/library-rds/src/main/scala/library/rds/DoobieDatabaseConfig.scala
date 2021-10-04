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

val io  = program1.transact(xa)
val io2 = program2.transact(xa)
val io3 = program3.transact(xa)
val io4 = program4.transact(xa)
val io5 = program5.transact(xa)
//val io6 = program6.quick
package library.rds

import doobie.*
import doobie.implicits.*

import cats.*
import cats.effect.*
import cats.implicits.*

import scala.concurrent.ExecutionContext

import cats.effect.unsafe.implicits.global

val xa = Transactor.fromDriverManager[IO](
  "com.mysql.cj.jdbc.Driver",
  "jdbc:mysql://127.0.0.1:13306/sample_doobie?useSSL=false",
  "takapi327",
  "takapi327"
)

val program1 = 42.pure[ConnectionIO]

val io = program1.transact(xa)

package ldbc

import cats.effect.IO
import cats.effect.unsafe.implicits.global

import lepus.database.DatabaseConfig

import doobie.*
import doobie.implicits.*
import lepus.doobie.specs2.*

class DoobieTest extends SQLSpecification:

  def databaseConfig: DatabaseConfig = DatabaseConfig("lepus.database://edu_todo/master")

  val xa = Transactor.fromDriverManager[IO](
    "com.mysql.cj.jdbc.Driver",
    "jdbc:mysql://127.0.0.1:13306/sample_doobie",
    "takapi327",
    "takapi327"
  )

  import java.time.*
  import doobie.implicits.javatimedrivernative.*
  case class Country(
    id:   Long,
    text: String,
    date: Year
  )

  "" should {
    "" in {
      check(
        sql"SELECT id, text, date FROM country_test".query[Country]
      )
    }
  }

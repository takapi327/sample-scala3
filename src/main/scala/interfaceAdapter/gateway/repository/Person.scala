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

class PersonRepository (
  using ec: ExecutionContext
) extends DoobieRepository:

  type PersonInfo = (String, Option[Short])

  def insertMany(ps: List[PersonInfo]): ConnectionIO[Int] =
    val sql = "insert into person (name, age) values (?, ?)"
    Update[PersonInfo](sql).updateMany(ps)

  def byName(pat: String) =
    transactor.use {
      sql"select name, code from country where name like $pat"
        .query[(String, String)]
        .to[List]
        .transact[IO]
    }.unsafeRunSync()

  def byName2(pat: String) =
    transactor.use {
      sql"select name, code from country where name like $pat"
        .query[(String, String)] // handler will be picked up here
        .to[List]
        .transact[IO]
    }.unsafeRunSync()

package library.rdb

import scala.concurrent.ExecutionContext

import doobie.hikari.*

import cats.effect.*

import com.zaxxer.hikari.HikariConfig

import library.util.Configuration

trait DoobieDatabaseConfig(using ec: ExecutionContext)
  extends HikariConfigBuilder:

  protected val transactor: Resource[IO, HikariTransactor[IO]] =
    HikariTransactor.fromHikariConfig(
      hikariConfig = hikariConfig,
      connectEC    = ec
    )

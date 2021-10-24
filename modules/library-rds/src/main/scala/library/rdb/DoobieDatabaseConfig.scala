package library.rdb

import scala.concurrent.ExecutionContext

import doobie.hikari.*

import cats.effect.*

import com.zaxxer.hikari.HikariConfig

import library.util.Configuration

trait DoobieDatabaseConfig[M[_]: Async](
  using ec: ExecutionContext
) extends HikariConfigBuilder:

  val transactor: Resource[M, HikariTransactor[M]] =
    for
      hikariConfig <- Resource.eval(buildConfig())
      transactor   <- HikariTransactor.fromHikariConfig[M](hikariConfig, ec)
    yield
      transactor
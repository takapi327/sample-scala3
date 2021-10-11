package library.rdb

import scala.concurrent.ExecutionContext

import doobie.hikari.*

import cats.effect.*

import com.zaxxer.hikari.HikariConfig

import library.util.Configuration

trait DoobieDatabaseConfig(using ec: ExecutionContext):
  private lazy val driverName: String = Configuration.config.getString("library.rdb.driver")
  private lazy val jdbcUrl:    String = Configuration.config.getString("library.rdb.url")
  private lazy val user:       String = Configuration.config.getString("library.rdb.user")
  private lazy val password:   String = Configuration.config.getString("library.rdb.password")

  private val hikariConfig = new HikariConfig()
  hikariConfig.setDriverClassName(driverName)
  hikariConfig.setJdbcUrl(jdbcUrl)
  hikariConfig.setUsername(user)
  hikariConfig.setPassword(password)

  protected val transactor: Resource[IO, HikariTransactor[IO]] =
    HikariTransactor.fromHikariConfig(
      hikariConfig = hikariConfig,
      connectEC    = ec
    )

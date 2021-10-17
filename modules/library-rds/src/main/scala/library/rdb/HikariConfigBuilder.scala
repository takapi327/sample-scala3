package library.rdb

import cats.effect.*

import com.zaxxer.hikari.HikariConfig

import library.util.Configuration

trait HikariConfigBuilder:
  private lazy val config = Configuration()

  private lazy val driverName: String = config.get[String]("library.rdb.driver")
  private lazy val jdbcUrl:    String = config.get[String]("library.rdb.url")
  private lazy val user:       String = config.get[String]("library.rdb.user")
  private lazy val password:   String = config.get[String]("library.rdb.password")

  private val hikariConfig = new HikariConfig()
  hikariConfig.setDriverClassName(driverName)
  hikariConfig.setJdbcUrl(jdbcUrl)
  hikariConfig.setUsername(user)
  hikariConfig.setPassword(password)
  hikariConfig.addDataSourceProperty("useSSL", false)

  def buildConfig[F[_]: Sync](): F[HikariConfig] =
    Sync[F].delay {
      hikariConfig
    }

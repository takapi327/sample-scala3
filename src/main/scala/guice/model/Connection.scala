package guice.model

import cats.effect.IO

import doobie.hikari.HikariTransactor

class Connection(val xa: HikariTransactor[IO])

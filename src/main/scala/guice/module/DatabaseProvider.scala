package guice.module

import javax.inject.{ Inject, Singleton, Provider }

import com.google.inject.{ AbstractModule, Guice }

class Database:
  def name: String = "Todo"

@Singleton
class DatabaseProvider extends Provider[Database]:
  lazy val get: Database = new Database


package ldbc

import javax.sql.DataSource

import cats.implicits.*

import cats.effect.{ Sync, Resource }

trait DataSourceResource[F[_]]:
  def getConnection: Resource[F, ConnectionResource[F]]

object DataSourceResource:

  def apply[F[_]: Sync](dataSource: DataSource): DataSourceResource[F] = new DataSourceResource[F]:
    override def getConnection: Resource[F, ConnectionResource[F]] =
      val acquire: F[ConnectionResource[F]] = Sync[F].blocking(dataSource.getConnection).map(ConnectionResource(_))
      val release: ConnectionResource[F] => F[Unit] = connection => connection.close()
      Resource.make(acquire)(release)

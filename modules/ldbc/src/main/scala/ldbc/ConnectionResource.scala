
package ldbc

import cats.implicits.*

import cats.effect.{ Sync, Resource }
import cats.effect.implicits.*

import java.sql.Connection

trait ConnectionResource[F[_]]:
  def prepareStatement(sql: String): Resource[F, PreparedStatement[F]]
  def setReadOnly(readOnly: Boolean): F[Unit]
  def setAutoCommit(autoCommit: Boolean): F[Unit]
  def close(): F[Unit]
  def commit(): F[Unit]
  def rollback(): F[Unit]

object ConnectionResource:

  def apply[F[_]: Sync](connection: Connection): ConnectionResource[F] = new ConnectionResource[F]:
    override def prepareStatement(sql: String): Resource[F, PreparedStatement[F]] =
      val acquire = Sync[F].blocking(connection.prepareStatement(sql)).map(PreparedStatement(_))
      val release: PreparedStatement[F] => F[Unit] = statement => statement.close()
      Resource.make(acquire)(release)

    override def setReadOnly(readOnly: Boolean): F[Unit] =
      Sync[F].blocking(connection.setReadOnly(readOnly))

    override def setAutoCommit(autoCommit: Boolean): F[Unit] =
      Sync[F].blocking(connection.setAutoCommit(autoCommit))

    override def close(): F[Unit] =
      if connection != null
        then Sync[F].blocking(connection.close())
        else Sync[F].unit

    override def commit(): F[Unit] =
      Sync[F].blocking(connection.commit())

    override def rollback(): F[Unit] =
      Sync[F].blocking(connection.rollback())

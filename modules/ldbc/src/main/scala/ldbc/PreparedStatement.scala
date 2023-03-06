
package ldbc

import cats.implicits.*

import cats.effect.Sync

trait PreparedStatement[F[_]]:
  def executeQuery(): F[ResultSet[F]]
  def close(): F[Unit]

  def setString(parameterIndex: Int, v: String): F[Unit]
  def setInt(parameterIndex: Int, v: Int): F[Unit]
  def setDouble(parameterIndex: Int, v: Double): F[Unit]

object PreparedStatement:

  def apply[F[_]: Sync](statement: java.sql.PreparedStatement): PreparedStatement[F] = new PreparedStatement[F]:
    override def executeQuery(): F[ResultSet[F]] =
      Sync[F].blocking(statement.executeQuery()).map(ResultSet(_))

    override def close(): F[Unit] =
      if statement != null
        then Sync[F].blocking(statement.close())
        else Sync[F].unit

    override def setString(parameterIndex: Int, v: String): F[Unit] =
      Sync[F].blocking(statement.setString(parameterIndex, v))

    override def setInt(parameterIndex: Int, v: Int): F[Unit] =
      Sync[F].blocking(statement.setInt(parameterIndex, v))

    override def setDouble(parameterIndex: Int, v: Double): F[Unit] =
      Sync[F].blocking(statement.setDouble(parameterIndex, v))

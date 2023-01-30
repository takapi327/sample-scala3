
package ldbc

import java.io.{ InputStream, Reader }
import java.sql.{ Date, SQLWarning, Time, Timestamp }
import java.time.LocalDateTime

import cats.implicits.*
import cats.effect.Sync

trait ResultSetLoader[F[_], A]:
  self =>

  def isOptional: Boolean = false

  def load(resultSet: ResultSet[F], columnLabel: String): F[A]

object ResultSetLoader:

  def apply[F[_], T](func: ResultSet[F] => String => F[T]): ResultSetLoader[F, T] =
    new ResultSetLoader[F, T]:
      override def load(resultSet: ResultSet[F], columnLabel: String): F[T] =
        func(resultSet)(columnLabel)

  given [F[_]]: ResultSetLoader[F, String]      = ResultSetLoader(_.getString)
  given [F[_]]: ResultSetLoader[F, Boolean]     = ResultSetLoader(_.getBoolean)
  given [F[_]]: ResultSetLoader[F, Byte]        = ResultSetLoader(_.getByte)
  given [F[_]]: ResultSetLoader[F, Array[Byte]] = ResultSetLoader(_.getBytes)
  given [F[_]]: ResultSetLoader[F, Short]       = ResultSetLoader(_.getShort)
  given [F[_]]: ResultSetLoader[F, Int]         = ResultSetLoader(_.getInt)
  given [F[_]]: ResultSetLoader[F, Long]        = ResultSetLoader(_.getLong)
  given [F[_]]: ResultSetLoader[F, Float]       = ResultSetLoader(_.getFloat)
  given [F[_]]: ResultSetLoader[F, Double]      = ResultSetLoader(_.getDouble)
  given [F[_]]: ResultSetLoader[F, Date]        = ResultSetLoader(_.getDate)
  given [F[_]]: ResultSetLoader[F, Time]        = ResultSetLoader(_.getTime)
  given [F[_]]: ResultSetLoader[F, Timestamp]   = ResultSetLoader(_.getTimestamp)
  given [F[_]]: ResultSetLoader[F, InputStream] = ResultSetLoader(_.getAsciiStream)
  given [F[_]]: ResultSetLoader[F, Object]      = ResultSetLoader(_.getObject)
  given [F[_]]: ResultSetLoader[F, Reader]      = ResultSetLoader(_.getCharacterStream)
  given [F[_]]: ResultSetLoader[F, BigDecimal]  = ResultSetLoader(_.getBigDecimal)

  given [F[_]: Sync](using loader: ResultSetLoader[F, Timestamp]): ResultSetLoader[F, LocalDateTime] with
    override def load(resultSet: ResultSet[F], columnLabel: String): F[LocalDateTime] =
      for
        result <- loader.load(resultSet, columnLabel)
      yield result.toLocalDateTime

  given [F[_]: Sync, A](using loader: ResultSetLoader[F, A]): ResultSetLoader[F, Option[A]] with

    override def isOptional: Boolean = true

    override def load(resultSet: ResultSet[F], columnLabel: String): F[Option[A]] =
      for
        result <- loader.load(resultSet, columnLabel)
        bool   <- resultSet.wasNull()
      yield if bool then None else Some(result)

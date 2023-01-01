package session

import cats.Functor
import cats.syntax.all.*

import cats.effect.{ Sync, Async }
import cats.effect.std.Random

import io.chrisdavenport.mapref.MapRef

trait SessionStorage[F[_], T]:

  def sessionId: F[SessionIdentifier]

  def getSession(id: SessionIdentifier): F[Option[T]]

  def modifySession[A](id: SessionIdentifier, func: Option[T] => (Option[T], A)): F[A]

object SessionStorage:

  def create[F[_]: Sync: Async, T](
  numShards: Int = 4,
  numBytes:  Int = 32,
  ): F[SessionStorage[F, T]] =
    for
      random <- Random.javaSecuritySecureRandom(numShards)
      ref    <- MapRef.inShardedImmutableMap[F, F, SessionIdentifier, T](numShards)
    yield new MemorySessionStorage[F, T](random, numBytes, ref)

  private class MemorySessionStorage[F[_]: Functor, T](
    random:   Random[F],
    numBytes: Int,
    access:   MapRef[F, SessionIdentifier, Option[T]]
  ) extends SessionStorage[F, T]:

    override def sessionId: F[SessionIdentifier] = SessionIdentifier.create(random, numBytes)

    override def getSession(id: SessionIdentifier): F[Option[T]] = access(id).get

    override def modifySession[A](id: SessionIdentifier, func: Option[T] => (Option[T], A)): F[A] =
      access(id).modify(func)

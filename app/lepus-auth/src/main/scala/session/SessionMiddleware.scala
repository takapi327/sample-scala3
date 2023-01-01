package session

import cats.{ Monad, Functor }
import cats.data.{ OptionT, Kleisli }
import cats.syntax.all.*

import org.http4s.*
import org.http4s.headers.`Set-Cookie`

object SessionMiddleware:

  type SessionRoutes[T, F[_]] = Kleisli[[A] =>> OptionT[F, A], ContextRequest[F, T], ContextResponse[F, T]]

  def optional[F[_]: Monad, A](
    storage:               SessionStorage[F, A],
    sessionIdentifierName: String         = "LEPUS_SESSION",
    httpOnly:              Boolean        = true,
    secure:                Boolean        = true,
    domain:                Option[String] = Option.empty[String],
    path:                  Option[String] = None,
    sameSite:              SameSite = SameSite.Lax,
    expiration:            ExpirationManagement[F] = ExpirationManagement.Static[F](None, None),
    mergeOnChanged:        Option[MergeManagement[Option[A]]] = Option.empty[MergeManagement[Option[A]]]
  )(routes: SessionRoutes[Option[A], F]): HttpRoutes[F] =
    val deleteCookie: `Set-Cookie` =
      `Set-Cookie`(
        ResponseCookie(
          name     = sessionIdentifierName,
          content  = "deleted",
          expires  = Some(HttpDate.Epoch),
          maxAge   = Some(-1L),
          domain   = domain,
          path     = path,
          sameSite = sameSite.some,
          secure   = secure,
          httpOnly = httpOnly
        )
      )

    def sessionCookie(id: SessionIdentifier): F[`Set-Cookie`] =
      expiration match
        case ExpirationManagement.Static(maxAge, expires) =>
          `Set-Cookie`(
            ResponseCookie(
              name     = sessionIdentifierName,
              content  = id.value,
              expires  = expires,
              maxAge   = maxAge,
              domain   = domain,
              path     = path,
              sameSite = sameSite.some,
              secure   = secure,
              httpOnly = httpOnly
            )
          ).pure[F]
        case e @ ExpirationManagement.Dynamic(fromNow) =>
          HttpDate.current[F](Functor[F], e.C).flatMap { now =>
            fromNow(now).map { case ExpirationManagement.Static(maxAge, expires) =>
              `Set-Cookie`(
                ResponseCookie(
                  name     = sessionIdentifierName,
                  content  = id.value,
                  expires  = expires,
                  maxAge   = maxAge,
                  domain   = domain,
                  path     = path,
                  sameSite = sameSite.some,
                  secure   = secure,
                  httpOnly = httpOnly
                )
              )
            }
          }

    Kleisli { (request: Request[F]) =>
      val sessionId = SessionIdentifier.extract(request, sessionIdentifierName)
      val session   = sessionId.flatTraverse(id => storage.getSession(id))

      for
        sessionOpt <- OptionT.liftF(session)
        response   <- routes(ContextRequest(sessionOpt, request))
        out        <- OptionT.liftF((sessionId, response.context) match
          case (None, None) => response.response.pure[F]
          case (Some(id), Some(context)) =>
            storage.modifySession(
              id,
              {
                now =>
                  val next: Option[A] = mergeOnChanged.fold(context.some) {mm =>
                    if !mm.eqv(sessionOpt, now) then mm.whenDifferent(now, context.some)
                    else context.some
                  }
                  (next, ())
              }
            ) >> sessionCookie(id).map(response.response.putHeaders(_))
          case (None, Some(context)) =>
            storage.sessionId.flatMap(id =>
              storage.modifySession(
                id,
                {
                  now =>
                    val next: Option[A] = mergeOnChanged.fold(context.some) { mm =>
                      if !mm.eqv(sessionOpt, now) then mm.whenDifferent(now, context.some)
                      else context.some
                    }
                    (next, ())
                }
              ) >> sessionCookie(id).map(response.response.putHeaders(_))
            )
          case (Some(id), None) =>
            storage.modifySession(
              id,
              {
                now =>
                  val next: Option[A] = mergeOnChanged.fold(Option.empty[A]) { mm =>
                    if !mm.eqv(sessionOpt, now) then mm.whenDifferent(now, Option.empty)
                    else None
                  }
                  (next, ())
              }
            ).as(response.response.putHeaders(deleteCookie))
        )
      yield out
    }

  trait MergeManagement[A]:
    def eqv(a1: A, a2: A): Boolean

    def whenDifferent(changedValue: A, valueContextWishesToSet: A): A

  sealed trait ExpirationManagement[F[_]]
  object ExpirationManagement:
    case class Static[F[_]](maxAge: Option[Long], expires: Option[HttpDate]) extends ExpirationManagement[F]

    case class Dynamic[F[_]](fromNow: HttpDate => F[Static[F]])(using val C: cats.effect.Clock[F])
      extends ExpirationManagement[F]

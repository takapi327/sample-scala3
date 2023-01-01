package session

import cats.{ Monad, Functor }
import cats.data.Kleisli
import cats.syntax.all.*

import org.typelevel.vault.{ Key, Vault }

import org.http4s.*

object VaultSessionMiddleware:

  case object VaultSessionReset:
    val key = Key.newKey[cats.effect.SyncIO, VaultSessionReset.type].unsafeRunSync()

  case class VaultKeysToRemove(list: List[Key[_]])
  object VaultKeysToRemove:
    val key = Key.newKey[cats.effect.SyncIO, VaultKeysToRemove].unsafeRunSync()

  def impl[F[_]: Monad, A](
    storage:               SessionStorage[F, Vault],
    sessionIdentifierName: String = "LEPUS_SESSION",
    httpOnly:              Boolean = true,
    secure:                Boolean = true,
    domain:                Option[String] = Option.empty[String],
    path:                  Option[String] = None,
    sameSite:              SameSite = SameSite.Lax,
    expiration:            SessionMiddleware.ExpirationManagement[F] = SessionMiddleware.ExpirationManagement.Static[F](None, None)
  )(routes: HttpRoutes[F]): HttpRoutes[F] =
    SessionMiddleware.optional(
      storage               = storage,
      sessionIdentifierName = sessionIdentifierName,
      httpOnly              = httpOnly,
      secure                = secure,
      domain                = domain,
      path                  = path,
      sameSite              = sameSite,
      expiration            = expiration,
      mergeOnChanged        = None
    )(transFormRoutes(routes))

  private def transFormRoutes[F[_]: Functor](routes: HttpRoutes[F]): SessionMiddleware.SessionRoutes[Option[Vault], F] =
    Kleisli { (contextRequest: ContextRequest[F, Option[Vault]]) =>
      val initVault = contextRequest.context.fold(contextRequest.req.attributes)(context =>
        contextRequest.req.attributes ++ context
      )
      routes.run(contextRequest.req.withAttributes(initVault))
        .map { response =>
          val outContext =
            contextRequest.context.fold(response.attributes)(context => response.attributes ++ context)
          outContext.lookup(VaultSessionReset.key)
            .fold(
              outContext.lookup(VaultKeysToRemove.key)
                .fold(
                  ContextResponse(outContext.some, response.withAttributes(outContext))
                )(toRemove =>
                  ContextResponse(
                    toRemove.list.foldLeft(outContext) { case (v, k) => v.delete(k) }.some,
                    response.withAttributes(outContext)
                  )
                )
            )(_ => ContextResponse(None, response.withAttributes(outContext)))
        }
    }
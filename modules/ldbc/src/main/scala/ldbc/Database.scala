
package ldbc

transparent trait Database:
  val path: String

  protected final def buildDatabaseConfig(replication: Option[String]): DatabaseConfig =
    replication.fold(DatabaseConfig(path))(v =>
      DatabaseConfig(path + s"/$v")
    )

object Database:

  trait Replication extends Database:
    final val write: DatabaseConfig = buildDatabaseConfig(Some("write"))
    final val read:  DatabaseConfig = buildDatabaseConfig(Some("read"))

  trait Single extends Database:
    final val single: DatabaseConfig = buildDatabaseConfig(None)

/*
 * example:
 * {{{
 *   class UserQuery:
 *     def findById(id: User.Id): Query[Option[User]] =
 *       sql"SELECT * FROM user WHERE id = $id".query[Option[User]]
 *
 *   class UserRepository(db: Database, query: UserQuery) =
 *     def findById(id: User.Id): IO[Option[User]] =
 *       db.readOnly(query.findById(id))
 *
 *       or
 *
 *       query.findById(id).readOnly
 *
 *       or
 *
 *       db.readOnly(
 *         for
 *           ...
 *           user <- sql"SELECT * FROM user WHERE id = $id".query[Option[User]]
 *         yield user
 *       )
 *
 *       or
 *
 *       db.transaction(
 *         for
 *           ...
 *           user <- sql"SELECT * FROM user WHERE id = $id".query[Option[User]]
 *         yield user
 *       )
 *
 *   class UserService(repository: UserRepository):
 *     def findById(id: User.Id): IO[Option[User]] =
 *       repository.findById(id)
 *
 *       or
 *
 *       repository.readOnly
 * }}}
 *
 */

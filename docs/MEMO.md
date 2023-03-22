# 概要

Slickのメモ

使用バージョン

- Scala 3.2.0
- Slick 3.5.0

## 型制御
case classのモデルとTableで定義されたカラムの型が一致していなくてもコンパイルでエラーにはならず、実行時にエラーとなる。

```scala
case class User(
  id:   String,
  name: String,
  age:  Option[Int],
)

class UserTable(tag: Tag) extends Table[User](tag, "user"):
  def id   = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def name = column[Int]("name")
  def age  = column[Option[Int]]("age")

  def * = (id.?, name, age).mapTo[User]

db.run(userTable.result.headOption)

```

## カラムタイプ制御

String型の値に数値型を表すカラムタイプを設定してもコンパイルでエラーとなることはない。
実行時エラーも発生しない。

このカラム型に関しては、JDBCで使用されるstatementを生成する場合にのみ使用される。

※ みんなが使用しているカラム型を表す値は、IxiaSが設定した[エイリアス](https://github.com/ixias-net/ixias/blob/develop/framework/ixias-core/src/main/scala/ixias/persistence/lifted/SlickColumnOptionOps.scala)でありSlick自体が提供しているものではない。

```scala
class UserTable(tag: Tag) extends Table[User](tag, "user"):
  def id   = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name", SqlType("BIGINT(64)"))
  def age  = column[Option[Int]]("age")

  def * = (id.?, name, age).mapTo[User]
```

上記設定で生成されたcreate statement文は以下のようになる。

```sql
create table `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `name` BIGINT(64) NOT NULL,
  `age` INTEGER
)
```

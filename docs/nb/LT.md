---
marp: true
---

# tapirのようなものをScala3 x JDBCで作りたい

---

# tapirとは

エンドポイントの記述とビジネスロジックを分離して記述できるScalaのライブラリ

https://github.com/softwaremill/tapir

---

# tapirを使うメリット

- ルーティング定義からOpenAPIドキュメントを自動生成できるので、ドキュメントの手動管理していると起こりがちな仕様と実装のずれを減らせる
- 実行サーバーの情報を持たないので、サーバーの実装に依存しにくいルーティング定義が書ける
- Interpreter モジュールを差し替えることでサーバーの実装を差し替えることもできる
- 型でしっかりモデリングされているのでコンパイラで間違いを検出できる
- APIクライアントとしても使用することができる

---

# 社内の課題

- プロダクト仕様書の運用
- テストの書きにくさ
- バックエンド/フロントエンドでモデル定義が乱立
- etc...

課題を解決できるのでは？と思い社内のプロダクトをScala2からScala3へ書き換えた時に使用してみました

---

# 実際にプロダクトで使ってみて

ルーティング定義からOpenAPIドキュメントを自動生成できる点はかなり良かった。
ルーティング定義にコメントを書くことができる点も良かった。確かに仕様と実装のずれを減らせると思った。

生成された仕様書からフロントで使用するAPIクライアントの自動生成をすることもできたので、APIクライアントやモデル定義を書く必要がなくなり記述量を減らせた。

サーバーの実装に依存しにくいルーティング定義が書けるおかげでコントローラーなどの実装もどのサーバーで実行されるのかという情報を隠蔽して実装することができた。

---

# こんな感じのものをJDBCでも作りたい

---

# 目指すもの

- 型でしっかりモデリングしてコンパイラで間違いを検出できる
- テーブル定義からドキュメントを自動生成できる
- 実行するライブラリを選べるようにする

---

# 型でしっかりモデリングしてコンパイラで間違いを検出できる

モデルを使ってTable定義を作成するようにする

```scala
case class User(
  id:        Long,
  name:      String,
  age:       Option[Int],
  updatedAt: LocalDateTime,
  createdAt: LocalDateTime
)
```

このモデルから作成するテーブルを作成する時には以下の制限がつけられるようにする
1. カラムの数はモデルのプロパティの数と同じになる
2. プロパティの型とカラムのデータタイプは、指定したものと同じになる
(Long => BIGINTは○ Long => VARCHARはXという感じ)

---

# カラム定義

カラムには型パラメーターを持たせる
データタイプや属性はこの型パラメーターと同じにものを受け取るようにしておく

```scala
trait Column[T]:

  /** Column Field Name */
  def label: String

  /** Column type */
  def dataType: DataType[T]

  /** Extra attribute of column */
  def attributes: Seq[Attribute[T]]

  /** Column comment */
  def comment: Option[String]
```

---

# データタイプ

データタイプはそれぞれ自身が受け取れる型に境界を設けておく
(複数の型を受け取れるデータタイプに関してはUnionタイプを使って定義する)
範囲などはinlineとcompiletimeを使用して制限を設けておく

```scala
inline def BIGINT[T <: Long](inline length: Int): Bigint[T] =
  inline if length < 0 || length > 255 then error("The length of the BIGINT must be in the range 0 to 255.")
  else Bigint(length)
```

---

# テーブル

テーブル定義は以下の機能を使って実装する
- Tuple
Tuple.Mapなどでタプルの各メンバの型 T を F[T] に変換する
- Mirror
モデルとタプルの相互変換を行う
- Dynamic
動的なメソッドを追加してカラム情報にアクセスできるようにする

以下を参考にさせていただきました。
https://speakerdeck.com/phenan/tuples-and-mirrors-in-scala3-and-higher-kinded-data

---

# Dynamic

ScalaのDynamicは、コードを書くときに変数やメソッドの型を宣言する必要がないという機能です。

例えば、以下のようなコードがあったとします。

```scala
val x = 10
val y = "Hello"
```

この場合、変数xはInt型であり、変数yはString型です。しかし、Dynamicを使うと、型宣言を省略できます。

---

```scala
import scala.language.dynamics

class MyDynamic extends Dynamic {
  def selectDynamic(name: String): String = s"Hello, $name!"
}

val myDynamic = new MyDynamic
println(myDynamic.world) // "Hello, world!"
```

この例では、MyDynamicクラスがDynamicトレイトを実装しています。selectDynamicメソッドをオーバーライドすることで、クラスに動的なメソッドを追加することができます。selectDynamicメソッドは、String型の引数を受け取り、String型の結果を返します。

---

また、動的なオブジェクトに対してメソッドを呼び出すこともできます。

```scala
import scala.language.dynamics

class MyDynamic extends Dynamic {
  def applyDynamic(name: String)(args: Any*): String =
    s"Calling method $name with arguments (${args.mkString(", ")})"
}

val myDynamic = new MyDynamic
println(myDynamic.sayHello("Alice", "Bob"))
// "Calling method sayHello with arguments (Alice, Bob)"

```
この例では、MyDynamicクラスがapplyDynamicメソッドをオーバーライドしています。applyDynamicメソッドは、String型のnameと、可変長引数のargsを受け取り、String型の結果を返します。

---

# テーブル

Tableを継承したモデルを定義しておく
引数のcolumnsは、Tuple.Mapを使用して型パラメーターのTupleをColumn型で受け取るようにしている

(Long, String)というTupleの型が渡された場合に渡せる引数の型は、(Column[Long], Column[String])というTuple型になる

```scala
object Table extends Dynamic:

  private case class Impl[P <: Product, T <: Tuple](
    name:           String,
    columns:        Tuple.Map[T, Column],
    keyDefinitions: Seq[Key]
  ) extends Table[P]:

    ...
```

---

フィールド名でのアクセスを可能にするためにselectDynamicを実装する

```scala
    override def selectDynamic[Tag <: Singleton](
      tag: Tag
    )(using
      mirror: Mirror.ProductOf[P],
      index:  ValueOf[Tuples.IndexOf[mirror.MirroredElemLabels, Tag]]
    ): Column[Tuple.Elem[mirror.MirroredElemTypes, Tuples.IndexOf[mirror.MirroredElemLabels, Tag]]] =
      columns
        .productElement(index.value)
        .asInstanceOf[Column[Tuple.Elem[mirror.MirroredElemTypes, Tuples.IndexOf[mirror.MirroredElemLabels, Tag]]]]
```

---

Singleton型を使用することで、特定の値を持つ唯一の型を表すことができるようになる

```scala
def single[Tag <: Singleton](x: Tag): Tag = x

val x = single("hello world")
// val x: String = hello world

val x = single[String]("hello world") // エラー
val x = single["hello world"]("hello world") // ok
val x = single[Singleton & String]("hello world") // ok

```

---

MirroredElemLabelsとTagが一致するIndexを生成し、ValueOfで値として扱えるようにする

```scala
override def selectDynamic[Tag <: Singleton](...)(
  ...
  index: ValueOf[Tuples.IndexOf[mirror.MirroredElemLabels, Tag]]
):

...

import scala.compiletime.ops.int.S

object Tuples:

  type IndexOf[T <: Tuple, E] <: Int = T match
    case E *: _  => 0
    case _ *: es => S[IndexOf[es, E]]
```
---

Tupleであるcolumnsから指定したIndexの値を取得する。
productElementの戻り値はAnyなため、Tuple.Elemを使用してTupleのIndexに対応した型に変更してあげる

Tuple.ElemはタプルXの位置Nにある要素の型を取得する型レベル関数

```scala
columns
  .productElement(index.value)
  .asInstanceOf[Column[Tuple.Elem[mirror.MirroredElemTypes, Tuples.IndexOf[mirror.MirroredElemLabels, Tag]]]]
```

---

```scala
object Table extends Dynamic:

  ...

  def applyDynamic[P <: Product](using
    mirror:    Mirror.ProductOf[P],
    converter: ColumnTupleConverter[mirror.MirroredElemTypes, Column]
  )(nameApply: "apply")(name: String)(columns: ColumnTuples[mirror.MirroredElemTypes, Column]): Table[P] =
    fromTupleMap[P](name, ColumnTupleConverter.convert(columns))

  private def fromTupleMap[P <: Product](using
    mirror: Mirror.ProductOf[P]
  )(
    _name:   String,
    columns: Tuple.Map[mirror.MirroredElemTypes, Column]
  ): Table[P] = Impl[P, mirror.MirroredElemTypes](_name, columns, Seq.empty)
```

---

# インスタンス生成

```scala

val table: Table[User] = Table[User]("user")(
  column("id", BIGINT(64), AUTO_INCREMENT, PRIMARY_KEY),
  column("name", VARCHAR(255)),
  column("age", INT(255).DEFAULT_NULL),
  column("updated_at", TIMESTAMP.DEFAULT_CURRENT_TIMESTAMP()),
  column("created_at", TIMESTAMP.DEFAULT_CURRENT_TIMESTAMP(true))
)
```

---

# インスタンス生成

モデル <-> テーブルマッピングでパラメーターの数が違っていたり、型が違うとcompileでエラーになる

![w:900](https://user-images.githubusercontent.com/57429437/231989552-7772f3cd-731b-403d-9203-3f0fb404ae39.png)

![w:500](https://user-images.githubusercontent.com/57429437/231989691-7208fac2-c5d7-41cc-a510-44a04ec422f0.png)

---

# インスタンス生成

Scalaの型とDBの型が一致していない場合もcompileでエラーとなる

![w:500](https://user-images.githubusercontent.com/57429437/231990164-63b523cd-44c3-440b-97b3-fbd3deafc57d.png)

データタイプの長さもDBの制限を超えるとcompileでエラーとなる

![w:900](https://user-images.githubusercontent.com/57429437/231990404-50be0339-28d6-4320-82e7-7a5261f49cf2.png)

---

# テーブル定義からドキュメントを自動生成できる

ドキュメント生成はSchemaSpyを使用

---

# SchemaSpy

データベースの情報を元に、ER図やテーブル、カラム一覧などの情報をHTML形式のドキュメントとして出力するツール

---

# SchemaSpy

maven対応していない。。。

しょうがないので、cloneしてゴニョゴニョして使ってたら

---

# SchemaSpy

つい最近maven対応しました！

https://github.com/schemaspy/schemaspy/issues/157

https://central.sonatype.com/artifact/org.schemaspy/schemaspy/6.2.2


---

# 実行するライブラリを選べるようにする

- 自作
- Slick

---

# 自作

フィールド名でのアクセス時にResultSetからカラム名を指定してデータ取得を行うResultSetReaderというものを暗黙的に渡してあげる

```scala
def applyDynamic[Tag <: Singleton](
  tag: Tag
)()(using
  mirror: Mirror.ProductOf[P],
  index:  ValueOf[Tuples.IndexOf[mirror.MirroredElemLabels, Tag]],
  reader: ResultSetReader[F, Tuple.Elem[mirror.MirroredElemTypes, Tuples.IndexOf[mirror.MirroredElemLabels, Tag]]]
): Kleisli[F, ResultSet[F], Tuple.Elem[mirror.MirroredElemTypes, Tuples.IndexOf[mirror.MirroredElemLabels, Tag]]] =
  Kleisli { resultSet =>
    val column = table.selectDynamic[Tag](tag)
    reader.read(resultSet, column.label)
  }
```

---

# 自作

ResultSet -> Userへの変換を行うKleisliを定義してあげる

```scala
given Kleisli[IO, ResultSet[IO], User] =
  for
    id        <- table.id()
    name      <- table.name()
    age       <- table.age()
    status    <- table.status()
    updatedAt <- table.updatedAt()
    createdAt <- table.createdAt()
  yield User(id, name, age, status, updatedAt, createdAt)
```

---

# 自作

StringContextなどでSQL文を生成して、DataSource -> Connection -> Statement -> ResultSetというようなJDBCの標準的なアクセスを行う (doobieのような感じ)

```scala
val user: IO[User] = sql"SELECT * FROM user".query.transaction.run(dataSource)
```

---

# Slick

強く型付けされ、高度に構成可能なAPIを持つ、Scalaのための高度で包括的なデータベースアクセスライブラリ

Scalaのコレクションを扱うようにデータベースを操作することができるのが特徴


※ 2023/04時点ではまだScala3対応版はリリースされていない。そのため対応進行中のものをクローンして使っています。

---

# Slick



---

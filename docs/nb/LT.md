---
marp: true
---

# tapirのようなものをJDBCで作りたい

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
- 実行するライブラリを切り替えられるようにする
- テーブル定義からSchemaSPYのドキュメントを自動生成できる

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

```scala
/** Trait for generating SQL table information.
  *
  * @tparam P
  *   A class that implements a [[Product]] that is one-to-one with the table definition.
  */
private[ldbc] trait Table[P <: Product] extends Dynamic:

  /** Table name */
  private[ldbc] def name: String

  /** Table Key definitions */
  private[ldbc] def keyDefinitions: Seq[Key]

  /** Methods for statically accessing column information held by a Table.
    *
    * @param tag
    *   A type with a single instance. Here, Column is passed.
    * @param mirror
    *   product isomorphism map
    * @param index
    *   Position of the specified type in tuple X
    * @tparam Tag
    *   Type with a single instance
    */
  def selectDynamic[Tag <: Singleton](
    tag: Tag
  )(using
    mirror: Mirror.ProductOf[P],
    index:  ValueOf[Tuples.IndexOf[mirror.MirroredElemLabels, Tag]]
  ): Column[Tuple.Elem[mirror.MirroredElemTypes, Tuples.IndexOf[mirror.MirroredElemLabels, Tag]]]
```

---

# テーブル

テーブル定義はProductの境界を持った型パラメータを受け取り、DynamicのselectDynamicメソッドを使用してしてしたカラムの情報を取得できるようにしておく

---

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
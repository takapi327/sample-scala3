# JDBCのConnectionオブジェクトに定義されているメソッドを試す

## StatementとPreparedStatementの違い

JDBC (Java Database Connectivity) は、JavaプログラムからデータベースにアクセスするためのAPIです。JDBCには、StatementとPreparedStatementの2つの主要なクラスがあります。

Statementクラスは、SQL文を直接実行します。つまり、SQL文の文字列を作成し、その文字列をデータベースに送信します。Statementを使用する場合、SQL文の文字列は毎回構築する必要があります。

PreparedStatementクラスは、SQL文を事前にコンパイルしておき、再利用できるようにします。つまり、SQL文を文字列として構築する代わりに、プレースホルダーを使用してSQL文を作成します。プレースホルダーは、後で変数の値を設定することができます。PreparedStatementを使用する場合、SQL文は事前にコンパイルされているため、データベースへのアクセスが高速になります。

PreparedStatementは、Statementと比較して次の利点があります。

- パフォーマンスが高い：PreparedStatementはSQL文を事前にコンパイルするため、同じSQL文を複数回実行する場合、データベースへのアクセスが高速になります。
- SQLインジェクション攻撃を防止する：PreparedStatementは、プレースホルダーを使用してSQL文を構築するため、SQLインジェクション攻撃を防止することができます。
- クエリの可読性が向上する：PreparedStatementを使用すると、SQL文がプレースホルダーを含む形式で構築されるため、可読性が向上します。

総じて、PreparedStatementを使用することで、より高速で安全なデータベースアクセスを実現することができます。

## nativeSQL

JDBC（Java Database Connectivity）は、Javaアプリケーションとデータベースを接続するためのAPIです。JDBCを使用してデータベースに接続する場合、Connectionオブジェクトを使用します。Connectionオブジェクトには、SQL文を実行するためのメソッドが用意されていますが、これらのメソッドはSQL文を解析して実行するため、データベースごとに異なるSQL構文に対応することができません。

そこで、Connectionオブジェクトには、SQL文を文字列として直接実行するためのメソッドとして、nativeSQL()メソッドが用意されています。nativeSQL()メソッドを使用することで、データベース固有のSQL構文を使用することができます。

nativeSQL()メソッドの使用方法は以下のようになります。

```java
Connection con = DriverManager.getConnection(url, user, password);
String nativeSql = con.nativeSQL("SELECT * FROM mytable WHERE mycolumn = ?");
```

nativeSQL()メソッドにSQL文を文字列として渡すと、そのSQL文を実行するためのデータベース固有のSQL文を返します。上記の例では、mytableというテーブルからmycolumnという列の値が?と一致するレコードを取得するSQL文を文字列として渡しています。このSQL文がMySQLであれば、SELECT * FROM mytable WHERE mycolumn = ?というまま実行されますが、Oracle Databaseであれば、SELECT * FROM mytable WHERE mycolumn = :1というSQL文に変換されます。

nativeSQL()メソッドを使用することで、データベースごとに異なるSQL構文を意識することなく、JDBCを使用してデータベースにアクセスすることができます。

## setCatalog

JDBC（Java Database Connectivity）は、Javaアプリケーションとデータベースを接続するためのAPIです。JDBCを使用してデータベースに接続する場合、Connectionオブジェクトを使用します。Connectionオブジェクトには、setCatalog()メソッドが用意されており、このメソッドを使用することで、接続先のデータベースを指定することができます。

setCatalog()メソッドの使用方法は以下のようになります。

```java
Connection con = DriverManager.getConnection(url, user, password);
con.setCatalog("mydatabase");
```

setCatalog()メソッドに接続先のデータベース名を文字列として渡すことで、そのデータベースに接続することができます。上記の例では、mydatabaseというデータベースに接続しています。

setCatalog()メソッドは、データベースごとにサポートされていない場合があります。その場合は、setCatalog()メソッドを呼び出しても何も起こりません。また、複数のデータベースに接続している場合には、setCatalog()メソッドを呼び出しても、現在アクティブな接続が変更されるわけではありません。接続先を変更する場合には、新しいConnectionオブジェクトを作成する必要があります。

```scala
object Example extends IOApp:

private val dataSource = new MysqlDataSource()
dataSource.setServerName("127.0.0.1")
dataSource.setPortNumber(13306)
dataSource.setDatabaseName("sample_doobie")
dataSource.setUser("takapi327")
dataSource.setPassword("takapi327")

private val acquire: IO[Connection[IO]] = IO.blocking(dataSource.getConnection).map(Connection(_))
private val release: Connection[IO] => IO[Unit] = connection => connection.close()
private val resource: Resource[IO, Connection[IO]] = Resource.make(acquire)(release)

override def run(args: List[String]): IO[ExitCode] =
  resource.use(conn => {
    for
      catalog <- conn.getCatalog()
      statement <- conn.prepareStatement("SELECT * FROM country WHERE code = ?")
      _ <- statement.setString(1, "USA")
      resultSet <- statement.executeQuery()
      _ <- resultSet.next()
      name <- resultSet.getString(2)
      _ <- conn.setCatalog("sample_doobie2")
      catalog2 <- conn.getCatalog()
      statement2 <- conn.prepareStatement("SELECT * FROM country WHERE code = ?")
      _ <- statement2.setString(1, "USA")
      resultSet2 <- statement2.executeQuery()
      _ <- resultSet2.next()
      name2 <- resultSet2.getString(2)
    yield
      println(catalog)
      println(catalog2)
      println(name)
      println(name2)
      ExitCode.Success
  })
```

```shell
[info] running (fork) ldbc.sql.Example 
[info] sample_doobie
[info] sample_doobie2
[info] Afghanistan
[info] Afghanistan2
```

## setTransactionIsolation

JDBC（Java Database Connectivity）は、Javaアプリケーションとデータベースを接続するためのAPIです。JDBCを使用してデータベースに接続する場合、Connectionオブジェクトを使用します。Connectionオブジェクトには、setTransactionIsolation()メソッドが用意されており、このメソッドを使用することでトランザクションの分離レベルを指定することができます。

setTransactionIsolation()メソッドの使用方法は以下のようになります。

```java
Connection con = DriverManager.getConnection(url, user, password);
con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
```

setTransactionIsolation()メソッドに分離レベルを表す整数値を渡すことで、トランザクションの分離レベルを指定することができます。また、Connectionクラスには、以下のような定数が用意されており、これらを使用することで分離レベルを指定することもできます。

- Connection.TRANSACTION_NONE: トランザクションをサポートしていない。
- Connection.TRANSACTION_READ_UNCOMMITTED: コミットされていない変更があるデータを他のトランザクションから読み取ることができる。
- Connection.TRANSACTION_READ_COMMITTED: コミットされた変更のみが他のトランザクションから読み取れる。
- Connection.TRANSACTION_REPEATABLE_READ: 同じクエリを実行しても常に同じ結果が返されるように、他のトランザクションからの変更によって影響を受けないようにする。
- Connection.TRANSACTION_SERIALIZABLE: トランザクション同士が直列化されているように扱い、データの整合性を保証する。

なお、トランザクションの分離レベルは、複数のトランザクションが同時にデータベースを更新する場合に、どの程度トランザクション同士を分離するかを指定するものです。分離レベルが高くなるほど、トランザクション同士を強く分離し、データの整合性を確保することができますが、同時にパフォーマンスも低下する可能性があります。

## getGeneratedKeys

getGeneratedKeys()メソッドは、データベースによって自動生成されたキーを取得するために使用されます。具体的には、INSERT文を実行した後に、そのテーブルに自動生成されたキーがある場合に、その値を取得することができます。

```scala
object Example extends IOApp:

  private val dataSource = new MysqlDataSource()
  dataSource.setServerName("127.0.0.1")
  dataSource.setPortNumber(13306)
  dataSource.setDatabaseName("sample_doobie")
  dataSource.setUser("takapi327")
  dataSource.setPassword("takapi327")

  private val acquire: IO[Connection[IO]] = IO.blocking(dataSource.getConnection).map(Connection(_))
  private val release: Connection[IO] => IO[Unit] = connection => connection.close()
  private val resource: Resource[IO, Connection[IO]] = Resource.make(acquire)(release)

  override def run(args: List[String]): IO[ExitCode] =
    resource.use(conn => {
      for
        statement <- conn.prepareStatement("INSERT INTO auto_inc_test VALUES (null, ?)", Statement.Generated.RETURN_GENERATED_KEYS)
        _ <- statement.setString(1, "takapi@exaple.com")
        _ <- statement.executeUpdate()
        resultSet <- statement.getGeneratedKeys()
        _ <- resultSet.next()
        id <- resultSet.getLong(1)
      yield
        println(id)
        ExitCode.Success
    })
```

```shell
[info] compiling 1 Scala source to /Users/takapi327/Development/oss/ldbc/module/ldbc-sql/target/scala-3.2.1/classes ...
[info] running (fork) ldbc.sql.Example 
[info] 7
```
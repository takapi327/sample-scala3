# 第8章. 効果の検証

テストというのは、とてつもなく複雑で微妙なテーマです。IOのような効果を議論してきましたが、IOを含むすべてのものをテストすることは、非常に自由度の高い提案です。

そのかわり、2つの分野に焦点を当てようと思います。
IO 効果によって生成された値をテストすることと、IO 効果が ExecutionContext のような実行時の依存関係とどのように相互作用するかを制御することです。

## 8.1. 効果的な値に関するアサーション

IO の考え方は、その実行を遅らせることなので、IO の値をテストするには、実行する必要があります。
実行し、それが生成する値についてアサーションする必要があります。
つまり、unsafeRunSync のような IO の安全でないメソッドを呼び出す必要があるのです。

```scala
def assertGreaterThanZero(i: IO[Int]) =
  assert(i.unsafeRunSync() > 0) // 1
```

1. unsafeRunSyncを使うことで、アサーション機構の選択を意図的に避けています。お好きなテストや "matchers "を使うことができます。

あるいは、アサーションを IO の「内部」で行い、構成された効果を確実に実行することもできます。

```scala
def assertGreaterThanZero(i: IO[Int]) =
- assert(i.unsafeRunSync() > 0)
+ i.map(j => assert(j > 0)).unsafeRunSync()
```

unsafeRunSync は、効果が失敗するかキャンセルされると例外を投げることを覚えておいてください。
テストフレームワークがスローされた例外を失敗として扱わない場合、あるいは
失敗またはキャンセルが起こったことを主張したい場合、attempt を使用して、成功の値や失敗・キャンセルの例外を、成功した Either の値として取り込むことができます。

```scala
def assertUnsuccessful[A](ia: IO[A]) =
  assert(ia.attempt.unsafeRunSync().isLeft)
```

### 8.1.1. インターフェースによる効果の偽造

IOを実行することで、その効果を発揮させることができます。
テスト時には、実際の効果を発揮させたくないとしたらどうでしょうか？たとえば、電子メールを送ったり、データベースを更新したりするような効果です。これは、あらゆる種類のテストにおいて、非常に一般的な問題です。
そして、最も一般的な解決策は、避けたい効果を、代替の実装を提供できるようなインターフェースの背後に隔離することです。
このメソッドをオーバーライドして「制御」することはできませんが、 効果のある操作を抽象化することで、制御することができます。

例えば、電子メールを送信するために直接エフェクトを作成するのではなく、インターフェイスのメソッドを呼び出して送信します。

```scala
- def send(to: EmailAddress, email: Email): IO[Unit] = ???
+ trait EmailDelivery {
+   def send(to: EmailAddress, email: Email): IO[Unit]
+ }
```

本物の実装と一緒に、テスト用の「偽の」実装を作ることができます。
EmailDeliveryのインスタンスを作成し、常に失敗するようにします。

```scala
class FailingEmailDelivery extends EmailDelivery {
  def send(to: EmailAddress, email: Email): IO[Unit] =
  IO.raiseError(new RuntimeException(s"couldn't send email to $to"))
}
```

そして、その実装を使用するコードのテスト時に、偽の実装を使用します。
例えば、ユーザー登録サービスの動作をテストする場合偽の実装使用することができます。

```scala
class UserRegistration(emailDelivery: EmailDelivery) { // 1
  def register(email: EmailAddress): IO[Unit] =
    for {
      _ <- save(email)
      _ <- emailDelivery.send(to, new Email(???))
    } yield ()
  private def save(email: EmailAddress): IO[Unit] = ???
}
```

1. インターフェイスを依存関係として渡すことで、本物か偽物かを選択することができます。

非常に基本的なテストは、「登録メールが送信できない場合は登録に失敗する」と主張するかもしれません。

```scala
def registrationFailsIfEmailDeliveryFails(email: EmailAddress) =
  new UserRegistration(new FailingEmailDelivery)
    .register(email)
    .attempt
    .map(result => assert(result.isLeft, s"expecting failure, but was $result"))
    .unsafeRunSync
```

## 8.2. 依存関係の制御によるテスト効果のスケジューリング

インターフェイスを使ったフェイクエフェクトは、コードのモジュール性やテスト容易性を高めるのに効果的です。
しかし、Cats Effect 自体のテストはどうでしょうか？
Cats Effect自体も同じようなテクニックを使っています。
ヘルパークラスでは、偽のTestContext[25]を使えば、テストしたいエフェクトのコードで、偽のExecutionContextとTimerインスタンスを使用し、エフェクトのスケジューリングを明示的に制御することができます。
そうすると、効果の相対的な実行順序について、アサーションを行うことができます。

ここでは、TestContext のインスタンスを作成してそのメンバをスコープに入れ、エフェクトが参照できるようにしています。

※ Cats Effect 2

```scala
import cats.effect.laws.util.TestContext
val ctx = TestContext() // 1
implicit val cs: ContextShift[IO] = ctx.ioContextShift // 2
implicit val timer: Timer[IO] = ctx.timer // 2
```

1. TestContextのインスタンス化。
2. コンテキストのContextShiftとTimerをスコープに入れる。

テストでは、この後、手動でエフェクトスケジューリングクロックを進めることができます。

```scala
val timeoutError = new TimeoutException
val timeout = IO.sleep(10.seconds) *> IO.raiseError[Int](timeoutError) // 1
val f = timeout.unsafeToFuture() // 2
// Not yet
ctx.tick(5.seconds) // 3
assertEquals(f.value, None) // 3
// Good to go:
ctx.tick(5.seconds) // 4
assertEquals(f.value, Some(Failure(timeoutError))) // 4
```

1. タイムアウトの効果は、10秒後にエラーが表示されることを確認します。
2. エフェクトを実行するために、エフェクトをFutureに変換し、制御可能なExecutionContextで実行されるようにスケジュールします。
3. ここで時計を5秒進め、「まだ値を生成していない」と断言します。
4. さらに5秒後、10秒間のスリープが終了し、エラーが発生するはずです。

## 8.3. 概要

1. エフェクトに条件を付けると、エフェクトが実行される必要があります。
2.  効果を利用した計算をテストするには、その生成に関するインターフェイスを抽象化することで、効果を「偽」ることができます。
3. エフェクトの実行順序についてアサーションを行うには、TestContextを使います。次に、エフェクトの実行をスケジュールします。Futureに変換し、その後、「いつ」効果が起こるかを主張するために「時計」を進める。

[25] TestContext は cats.effect.laws パッケージに存在し、これは Cats Effect のコア部分には含まれません。依存関係にある。laws" モジュールの sbt 依存関係は、 "org.typelevel" %% "cats-effect-laws" %% となります。"2.1.3".

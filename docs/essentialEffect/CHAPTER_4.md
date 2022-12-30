# 第4章. 同時進行の制御

これまでのところ、我々はどちらかといえば不透明な効果を扱ってきた。効果を記述し、最終的にそれを実行して値（あるいはエラー）を生成することはできる。しかし，実行中の計算を制御する方法はまだない．

```scala
val i1: IO[A] = ??? // 1
val i2: IO[B] = ??? // 1
val i3: IO[C] = doSomething(i1, i2) // 1
val c: C = i3.unsafeRunSync() // 2
```

1. これらの効果はまだ始まっていません。私たちは、彼らが計算する内容を説明しただけです。
2. 私たちは、計算が完了したときに、その結果を得ることができます。私たちはその計算方法にアクセスできないので、それに影響を与える（コントロールする）ことはできない。

計算が実行されている可能性があるため、それを制御するということは、その計算と同時進行で行動することになるのです。この章では、同時実行中の効果をフォークして結合する方法、同時実行中の効果をキャンセルする方法、そして複数の効果を同時にレースする方法について説明します。


#### 並行処理と並列処理

よく混同されますが、並行処理と並列処理は別個の概念です。

並行: 計算の実行時間が重なる場合、同時並行となります。
並列: 計算が並列に行われるのは、その実行が同じ瞬間に行われる場合である。

つまり、並行処理が計算の構造や寿命の並び方を見るものであるのに対し、並列処理は実行中のリソースの運用を見るものである。
q
例えば、2つのスレッドを使えば、2つの計算を並列に（同時に！）実行することができます。

しかし、1つのスレッドで2つの計算を同時に実行することもできます。

一方を「一時停止」して、同じスレッドを使ってもう一方に切り替え、その逆も可能であれば、同時実行になります。

![parallel_thread.png](./images/parallel_thread.png)

2つのスレッドにより、ハイライトされた期間中、計算が並行して実行される。また、同時に実行されている。

![concurrent_thread.png](./images/concurrent_thread.png)

計算の中断と再開が可能であれば、1つのスレッドだけで2つの計算を同時に実行することができる。

並行処理では、計算の非決定性を重視します。いつ何が起こるかわからない、ただその寿命が重なっているということです。一方、並列処理には決定性が必要です。リソースがいくつあっても、同じ答えを出さなければなりません。

## 4.1. parMapN の動作を分解する。

同時進行するエフェクトのフォーク、ジョイン、キャンセルを実演するため、それぞれを含む独自の parMapN を作成します。

```scala
def myParMapN[A, B, C](ia: IO[A], ib: IO[B])(f: (A, B) => C): IO[C] = ???
```

myParMapNは、parMapNと同じように、以下が必要です。

- [ ] iaとibの計算を同時に開始する（"フォーク "する）。
- [ ] 各結果を待つ。
- [ ] iaまたはibが失敗した場合、"その他 "の効果をキャンセルする。
- [ ] 最後にf関数で結果を結合する。

(ここでは parMapN の 2 つの引数のバリエーションのみを記述し，他のアリティを無視しています)。

ここで重要なのは、「待つ」と「キャンセル」するためには「待つ」と「キャンセル」するための何かが必要だということです。

「待つ」「キャンセルする」ための何か、つまり「開始された」計算に対する一種のハンドルが必要だということです。キャッツ・エフェクトではでは、その概念はファイバーである。

## 4.2. ファイバーによる制御の獲得

以下のような式を書くと

```scala
for {
  result <- effect
  ...
```

この場合、値の結果は、効果によって生成された時点で初めて存在します。私たちは本質的に計算を続けるために結果が利用可能になるまで待っているのです。
結果を待つ代わりに、効果をフォークすることができます。
効果は開始されますが、その完了を待つことには興味がありません[11]。
しかし、フォークした結果は、フォークされた効果を管理するための値、つまりファイバーになります。[12]

Cats Effectでは、エフェクトをフォークするためにstartメソッドを使用します。では、簡単なものを作り、その挙動を調べてみましょう。

```scala
object Start extends IOApp:

  override def run(args: List[String]): IO[ExitCode] =
    for
      _ <- task.start // 1
      _ <- IO("task was started").debug // 2
    yield ExitCode.Success

  val task: IO[String] =
    IO("task").debug
```

1. 現在のエフェクトから実行をフォークするためにエフェクトを開始します。
2. タスクを開始した直後に、コンソールに何かを表示します。

Startを実行すると、次のような出力が表示されます。

```shell
[info] [io-compute-6] task // 1
[info] [io-compute-3] task was started
```

1. io-compute-6スレッドでのタスク実行の効果は、以下の効果とは異なることに注意してください。

エフェクトを開始すると、その実行は「フォーク」され、別のスレッドに移行します。

以下は、スタートの（簡略化した）サインである。[13]

```scala
def start: IO[Fiber[IO, A]]
```

この戻り値の型は興味深いもので、Fiberというデータ型を返すことで、start-edの効果に作用させることができるのです。
しかし、なぜstartはIOの中にあるFiberを返すのでしょうか？

IOの中にあるFiberを返しているのは、もしFiberを直接生成してしまうと、元のIOが今走っていることになりますが、実際には走っていません。
IO は明示的に実行されたときだけ実行されます。
ファイバーをエフェクトに包むことで、ソースIOが実行されるまで、ファイバーへのアクセスを遅らせる必要があります。

さて、Fiber のフォークを実演してみましたが、ここで警告を発しておく必要があると思います。
ファイバーは、並行制御のための非常に「低レベル」なメカニズムです。
同時実行性と並列性を実現するためには、絶対に必要なものです。
並行性と並列性を実現するためには絶対に必要なものですが、開発者としては、より高度な抽象化と操作を行うことで、より良い目標を達成できることが多いのです。

### 4.2.1. 続・myParMapN：フォークの効果

start を使って同時進行のエフェクトをフォークすることができるので、myParMapN に使ってみましょう。

```scala
def myParMapN[A, B, C](ia: IO[A], ib: IO[B])(f: (A, B) => C): IO[C] =
  for {
  fiberA <- ia.start // 1
  fiberB <- ib.start // 1
  } yield ??? // 2
```

1. それぞれのエフェクトを起動して同時進行させるのです。
2. その結果をどのように集めるか、あるいはキャンセルする可能性があるかは、まだわかりません。

ここでは、要求事項に対する私たちの進捗状況を紹介します。

- [x] iaとibの計算を同時に開始する（"フォーク "する）。
- [ ] 各結果を待つ。
- [ ] iaまたはibが失敗した場合、"その他 "の効果をキャンセルする。
- [ ] 最後にf関数で結果を結合する。

### 4.2.2. 実行中のファイバーに参加する

IO[A] 値で start を呼び出すと、Fiber[IO, A] 値を受け取ります。これによってIO[A]計算の実行について話すことができます．

ファイバーで何ができるのか？まず最初にできることは、結合することです。
フォークされた IO 効果の結果を返します。ファイバーが与えてくれた制御をあきらめることになります。
その結果、前にフォークされた値の最終的な結果についてだけ話すことができます。

```scala
val joined: IO[String] =
  for {
  fiber <- IO("task").start
  s <- fiber.join
  } yield s
```

起動したばかりのFiberに参加するとどうなるのか？何がどのスレッドで実行されるのか？

```scala
object JoinAfterStart extends IOApp:

  override def run(args: List[String]): IO[ExitCode] =
    for
      fiber <- task.start
      _     <- IO("pre-join").debug
      _     <- fiber.join.debug // 2
      _     <- IO("post-join").debug
    yield ExitCode.Success

  val task: IO[String] =
    IO.sleep(2.seconds) *> IO("task").debug // 1
```

1. タスクの遅延とデバッグを導入することで、タスクの同時制御（ファイバーの使用）とタスク自体を区別することができます。 タスクの同時制御（Fiberの使用）とタスクそのものを区別できるようにします。
2. pre-joinメッセージを表示した後、joinを呼び出します。

JoinAfterStartの出力を実行中。

※ Cats Effect 3から挙動が変わってる

```shell
[info] [io-compute-7] pre-join
[info] [io-compute-5] task // 1
// [info] [io-compute-5] task // Cats Effect 2の場合はこっちが出力される
[info] [io-compute-5] Succeeded(IO(task))
[info] [io-compute-5] post-join
```

1. タスクは「pre-join」の出力とは別のスレッドにあることに注意してください。

※ Cats Effect 2の場合
また、taskが2回プリントされているのがわかります。1回はIO("task").debugで、もう1回は
fiber.join.debugです。

Fiber に参加すると、Fiber が実行されていたスレッドで実行が継続されます。

### 4.2.3. 続・myParMapN：フォークされたエフェクトの結合

joinを使用した同時実行効果の結果を待つことができることがわかったので、myParMapNメソッドを更新することができます。
変換関数fを呼び出すには両方の結果が必要なのでどの順番でjoinするかは問題ではありません。しかしフォークされた両方のタスクを結合する必要があります。

```scala
def myParMapN[A, B, C](ia: IO[A], ib: IO[B])(f: (A, B) => C): IO[C] =
  for {
  fiberA <- ia.start
  fiberB <- ib.start
  a <- fiberA.join // 1
  b <- fiberB.join // 2
  } yield f(a, b) // 3
```

1. ファイバーAでフォークされたiaの結果を待つ。
2. ファイバーBでフォークされたibの結果を待つ。 
3. 両方が揃ったら、目的の値を計算する。

ここでは、要求事項に対する私たちの進捗状況を紹介します。

- [x] iaとibの計算を同時に開始する（"フォーク "する）。
- [x] 各結果を待つ。
- [ ] iaまたはibが失敗した場合、"その他 "の効果をキャンセルする。
- [x] 最後にf関数で結果を結合する。

やはりキャンセルが必要です。

## 4.3. 実行中のファイバーをキャンセルする

Fiberでできることの2つ目は、「キャンセル」です。

```scala
def cancel: cats.effect.CancelToken[IO]

type CancelToken[F[_]] = F[Unit] // 1
```

1. ファイバーのキャンセルは、それ自体が効果です。エフェクトをキャンセルすると、ユニット値が生成されます。

なぜ、実行中のタスクを止めたいと思うのだろうか。通常それは、その計算がもう必要ないことを示す情報を知ったからです。例えば（比較的遅い）データストアからのフェッチを開始するかもしれませんが、ユーザーが全体の処理をキャンセルすることを決定した場合、そのデータストアへのフェッチをキャンセルする必要があります。

実行中のFiberをキャンセルする基本的な例を挙げてみましょう。

```scala
object Cancel extends IOApp:

  override def run(args: List[String]): IO[ExitCode] =
    for
      fiber <- task.onCancel(IO("i was cancelled").debug.void).start // 1
      _     <- IO("pre-cancel").debug
      _     <- fiber.cancel // 2
      _     <- IO("canceled").debug
    yield ExitCode.Success

  val task: IO[String] =
    IO("task").debug *>
      IO.never // 3
```

1. エフェクトがキャンセルされた場合にコンソールに出力する onCancel コールバックを追加しています。 onCancelはimportで提供されるIOの拡張メソッドです。`cats.effect.implicits.*`
2. ファイバーは開始後にキャンセルします。 
3. IO.neverは、ビルトインの非終端効果です。これは IO[Nothing] 型を持ち Nothing 型は値を持たない型なので、このエフェクトは決して完了することができません。 しかし、キャンセルすることはできます。

実行中 キャンセル出力。

```shell
[info] [io-compute-4] task
[info] [io-compute-8] pre-cancel
[info] [io-compute-4] i was cancelled
[info] [io-compute-8] canceled
```

cancelはべき乗であることに注意してください。複数回実行しても、1回実行したのと同じ効果があります。 キャンセルされたタスクはキャンセルされ続けます。
しかし、キャンセルした後に参加すると、結果が生成されないため、参加は決して終了しません。

### 4.3.1. キャンセルはどのように行われるのですか？

エラーを発生させる効果と同時に長寿命な効果がある状況を設定してみましょう。
エラーが発生する効果がある場合を考えてみましょう。前者については、以前書いた
"ticking clock "を使います。

```scala
val tickingClock: IO[Unit] =
  for {
  _ <- IO(System.currentTimeMillis).debug
  _ <- IO.sleep(1.second)
  _ <- tickingClock
  } yield ()
```

parTupledを使った失敗するエフェクトと同時進行させるのです。

```scala
val ohNoes =
  IO.sleep(2.seconds) *> IO.raiseError(new RuntimeException("oh noes!")) // 1
val together =
  (tickingClock, ohNoes).parTupled
```

1. 2秒後にエラーを発生させ、カチカチ音を立てる時計がコンソールに数回表示される機会を与える。 コンソールに数回表示します。

例外が発生すると，tickingClock は何らかのエラーハンドラによってキャンセルされます．

together effectを実行すると、こうなります。

```shell
[info] 1671266509249
[info] 1671266510675
[info] 1671266511679
[error] Exception in thread "main" java.lang.RuntimeException: oh noes!
[error]         at essentialEffect.Cancel$package$.together(Cancel.scala:54)
[error]         at essentialEffect.together.main(Cancel.scala:46)
[error]         at *> @ essentialEffect.Cancel$package$.together(Cancel.scala:54)
[error] Nonzero exit code returned from runner: 1
[error] (Compile / run) Nonzero exit code returned from runner: 1
```

延々と繰り返されるtickingClockのエフェクトが停止し、明示的に何かをしたわけではありません。
では、キャンセルはどのように行われるのでしょうか？また、エフェクトはキャンセルされたかどうかを「知る」ことができるのでしょうか？
キャンセルされたことを知り、その情報に反応することができるでしょうか？

キャンセレーションを定義するために、Cats Effectはキャンセレーションバウンダリーという概念を使っています。
エフェクトの実行中に、キャンセル境界（それが何であれ）に遭遇した場合、現在のエフェクトのキャンセル状態がチェックされます。
キャンセルが発生した場合、現在のエフェクトのキャンセル状態がチェックされ、 そのエフェクトがキャンセルされていれば、実行が停止します。

ある観点からは、Cats Effect自体がエフェクトの実行中に定期的にキャンセル境界を挿入するため、キャンセルは「自動的」であると言える[14]。
また、以下の方法でキャンセル境界を「手動で」挿入することもできます。 `IO.cancelBoundary`。[15]

### 4.3.2. myParMapNの継続：キャンセルオンエラーの動作

一方の効果中にエラーが発生した場合、「もう一方の」繊維をキャンセルする必要があります。
それぞれのエフェクトを処理するために、onError コンビネータを使いましょう。

```scala
def myParMapN[A, B, C](ia: IO[A], ib: IO[B])(f: (A, B) => C): IO[C] =
  for {
  fiberA <- ia.start
  fiberB <- ib.start
  a <- fiberA.join.onError(_ => fiberB.cancel) // 1
  b <- fiberB.join.onError(_ => fiberA.cancel) // 2
  } yield f(a, b)
```

1. 繊維Aの計算に誤りがある場合、繊維Bをキャンセルする。
2. ファイバーBの計算に誤りがある場合、ファイバーAをキャンセルする。

しかし、ここには重大なバグがあります。何だかわかりますか？

問題は、onError ハンドラを登録すること自体が効果であるため、上記のコードではハンドラは登録されるだけです。
しかし、そうすると、onErrorハンドラを登録するのはfiberB を fiberB の結果で登録することはありません。

```scala
def myParMapN[A, B, C](ia: IO[A], ib: IO[B])(f: (A, B) => C): IO[C] =
  for {
  fiberA <- ia.start
  fiberB <- ib.start
  a <- fiberA.join.onError(_ => fiberB.cancel)
  b <- fiberB.join.onError(_ => fiberA.cancel) // 1
  } yield f(a, b)
```

1. ファイバーBのonErrorハンドラは、ファイバーAが完了するまで登録されません。

代わりに、両方のonErrorハンドラが登録されていることを確認する必要があります。もしを書くことができれば

```scala
for {
  fa <- ia.start
  fb <- ib.start
  faj = fa.join.onError(_ => fb.cancel)
  fbj = fb.join.onError(_ => fa.cancel)
  c <- myParMapN(
    fa.join.onError(_ => fb.cancel),
    fb.join.onError(_ => fa.cancel)
  )(f)
} yield c
```

しかし、それでは私たちが書こうとしているメソッドを使っていることになるのです (そしてそれは を使うことになります。） もし私たちが次のような「賢い」方法を試したら

```scala
for {
  fa <- ia.start
  fb <- ib.start
  faj = fa.join.onError(_ => fb.cancel)
  fbj = fb.join.onError(_ => fa.cancel)
  registerA <- faj.start // 1
  registerB <- fbj.start // 1
  a <- registerA.join
  b <- registerB.join
  c = f(a, b)
} yield c
```

1. フォークして両方のonErrorハンドラの登録を試みる（再度）

これもまた、キャンセルを適切に処理できません。 キャンセルされると、その後の結合は決して完了しません。

キャンセルされる可能性のある効果に対するjoinを避ける必要があります。
ここでは、どちらかの効果が先にキャンセルされる可能性があり、どちらがキャンセルされたかはわかりません。ファイバーAPI
は表現力が乏しいため、必要な情報を得ることができません。この問題を解決するには
2 つのエフェクトのレースを行い、どちらのエフェクトが先に終了したかを知ることができます。
どちらのエフェクトが先に終了したかを知ることで、その後にもう一方のエフェクトに参加することができます。
その結果、どちらの効果が先に終了したかを知ることができ、その後もう一方の効果に参加することができます。

## 4.4. レーシングマルチエフェクト

parMapN で複数の効果を同時に合成する場合、同時に実行されるすべての効果について、集まった出力を変換する関数を提供します。
その代わりに、最初に完了した効果にのみ興味を持ち、それらを時間的に関連付けるとしたらどうでしょう。
これをレースと呼び、IO.race を使ってレースが発生するようにします。 コンビネータを使うことで実現できます。

```scala
def race[A, B](lh: IO[A], rh: IO[B])(implicit cs: ContextShift[IO]): IO[Either[A, B]]
```

raceはparTupledと関連して考えることができます-どちらも効果を同時に実行します。
しかし，parTupledは両方の結果（1番目と2番目）を与えるのに対して，raceは片方（1番目か2番目）しか与えません．

```scala
val ia: IO[A] = ???
val ib: IO[B] = ???
(ia, ib).parTupled // IO[(A, B)] 1
IO.race(ia, ib) // IO[Either[A, B]] 2
```

1. 生成された (A, B) は A と B である． 
2. 生成された Either[A, B] は A か B のどちらかである。

特に有用なレースは、効果のタイムアウトです。
スリープが主効果より先に終了した場合、タイムアウトが発生します。

```scala
object Timeout extends IOApp:

  override def run(args: List[String]): IO[ExitCode] =
    for
      done <- IO.race(task, timeout) // 1
      _ <- done match { // 2
        case Left(_) => IO(" task: won").debug // 3
        case Right(_) => IO("timeout: won").debug // 4
      }
    yield ExitCode.Success

  val task: IO[Unit] = annotatedSleep("task", 100.millis) // 6
  val timeout: IO[Unit] = annotatedSleep("timeout", 500.millis)

  def annotatedSleep(name: String, duration: FiniteDuration): IO[Unit] =
    (
      IO(s"$name: starting").debug *>
        IO.sleep(duration) *> // 5
        IO(s"$name: done").debug
    ).onCancel(IO(s"$name: cancelled").debug.void).void
```

1. IO.raceは2つの効果を競わせ、先に終了した方の値を返します。またはキャンセルされます。
2. done は Either[Unit, Unit] 型の値である。それぞれのケースに対応するEitherでパターンマッチを行い，それぞれのケースを処理する。
3. 左であれば、タスクが先に終了し、タイムアウトが解除された。
4. Rightであれば、先にタイムアウトが終了し、タスクはキャンセルされました。
5. ここでは、与えられた持続時間に対して、睡眠効果を生み出す
6. ここでデュレーションを1000.millisに変更するとどうなるか？

このプログラムを実行すると、プリントアウトされます。

```shell
[info] [io-compute-7]  task: starting
[info] [io-compute-3] timeout: starting
[info] [io-compute-1]  task: done
[info] [io-compute-1] timeout: cancelled
[info] [io-compute-1]  task: won
```

このパターンは非常に一般的なので、組み込みのコンビネータがあります。IO.timeout。上記の例は以下のように書き換えることができます。

```scala
+ _ <- task.timeout(500.millis) // 1
- done <- IO.race(task, timeout)
- _ <- done match {
- case Left(_) => IO(" task: won").debug
- case Right(_) => IO("timeout: won").debug
- }
```

1. 効果がタイムアウト時間より長い場合、java.util.concurrent.TimeoutExceptionを発生させます。

もし、タイムアウトが発生したときに、その効果だけをキャンセルするのではな く、動作させたいのであれば
IO.timeoutTo メソッドを使用すると、タイムアウトが発生したときに評価する代替の IO 値を指定することができます。
このメソッドでは、タイムアウトが発生したときに評価する代替の IO 値を指定できます。

### 4.4.1. 自動キャンセルがないレース

IO.race は，より単純な IO.racePair というコンビネータをベースに作られており，このコンビネータは「負ける」効果をキャンセルすることができません．
その代わり、「勝ち」の値を「負け」の繊維と一緒に受け取るので、それをどうするかは自分で決めることができます。

```scala
def racePair[A, B](lh: IO[A], rh: IO[B])(implicit cs: ContextShift[IO]):
  IO[Either[(A, Fiber[IO, B]), (Fiber[IO, A], B)]] // 1
```

1. どちらかの効果にエラーが発生した場合、もう一方の効果はキャンセルされます

racePair を使えば，以下のようなエラー時のキャンセル実装を完成させることができます．

```scala
def myParMapN[A, B, C](ia: IO[A], ib: IO[B])(f: (A, B) => C): IO[C] =
  IO.racePair(ia, ib).flatMap {
    case Left((a, fb)) => (IO.pure(a), fb.join).mapN(f)  // 1
    case Right((fa, b)) => (fa.join, IO.pure(b)).mapN(f) // 1
  }
```

1. エラーが発生しなければ、どちらが先に終了したかを検出し、IO.pure を使って値をラップします。（すでに計算されているので IO.delay を使う必要はありません）最後に、IOの値を関数fで結合します。

myParMapNを終了します。

- [x] iaとibの計算を同時に開始する（"フォーク "する）。
- [x] 各結果を待つ。
- [x] iaまたはibが失敗した場合、"その他 "の効果をキャンセルする。
- [x] 最後にf関数で結果を結合する。

racePairにキャンセルを登録してもらうのは、ちょっとずるいと感じても、それはそれでいいんです。
そう感じるのは当然です。Fiber 自体では、エラー時のキャンセルを実装するのに十分な制御を与えてくれません。

## 4.5. まとめ

1. 並行処理により、実行中の計算を制御することができる
2. Fiber は、この制御へのハンドルです。並列計算を開始した後、キャンセルしたり、参加したりすることができます。
3. 同時に実行されているエフェクトはキャンセルすることができます。キャンセルされたエフェクトは、暗黙的または明示的なキャンセル境界によって実行を停止することが期待される。
4. 誰が先にゴールしたかを知るために、2回の計算を競うことができる。タイムアウトのような高次の効果もレースで構築できる。

[11] 分岐という言葉は、例えば川が2つの流れに分かれる地点を連想させるものである。一方は新しく始まった効果で、もう一方は以前から実行されていた効果の継続である。
[12] Fiberという用語は、threadという用語と似ているが異なるバリエーションとして選ばれた。Cats Effect.のファイバーは、実行中に使用されるスレッドとは論理的に別のものです。 しかし、キャッツ・エフェクトにおけるファイバーは、実行中に使用されるスレッドとは論理的に別のものである。
[13] Cats Effect 2 では、start は暗黙のうちに ContextShift パラメータを受け取り、これは最終的にエフェクトが実行されるスレッドプールを表します。Cats Effect 3 では、start メソッドは IO[FiberIO[A]] 型を返し、FiberIO[A] は基本的な Fiber[IO, Throwable, A] 型のエイリアスです。基本的な Fiber[IO, Throwable, A] 型のエイリアスで、3 つの型パラメータを持ちますが、Cats Effect 2 では 2 つの型パラメータでした。 パラメータが 3 つあるのに対し、Cats Effect 2 では 2 つです。
[14] 「キャッツ・エフェクト2」では、512回FlatMapを呼び出すごとにキャンセル境界が挿入される。キャッツ・エフェクト3では、すべての flatMapがキャンセル境界として扱われる。
[15] flatMap自体がキャンセル境界として定義されているため、Cats Effect 3ではIO.cancelBoundaryは削除されました。

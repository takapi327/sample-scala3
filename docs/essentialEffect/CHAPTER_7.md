# 第7章 リソースの管理

状態を管理する必要があることはよくあることです。
例えば、スレッドプールを使用する前に割り当てと設定を行う必要があり、それが終わると
スレッドをシャットダウンする必要があります。他の多くの種類の状態にも、同じようにライフサイクル管理の必要性があります。

- ネットワーク接続は、あるソケット・ネットワーク抽象化を介してリモート・システムとの接続を維持します。ソケットの割り当ては、実際に（リモート）接続を確立するのに必要な時間に加えて、高価になることがあります。またソケットは不要になったら回収する必要があります。
- データベース接続は、ネットワーク接続と同様に、リモートシステムと通信する必要があり、前の例と同様のコストがかかります。また接続プロトコルに関わるスレッドなどの追加リソースを独自に管理することもあります。

Cats Effectでは、Resourceデータ型がこの獲得-使用-解放のパターンを表し、状態を管理します。ここでは、Resourceの値を独自に作成する方法と、Resourceを合成する方法について説明します。そして、その値をアプリケーションでどのように使用するかについて学びます。

## 7.1. 状態を管理するResourceの作成

ある状態のライフサイクルを管理する方法を理解するために、Resourceを作成しましょう。
ここではResource.makeを使用します。このメソッドは2つの有効な引数を取ります：1つは状態を生成（獲得）するため、もう1つはそれを解放するためです。

```scala
def make[A](acquire: IO[A])(release: A => IO[Unit]): Resource[IO, A]
```

そして、そのResourceをプログラムで使用することになります。

```scala
object BasicResource extends IOApp:

  override def run(args: List[String]): IO[ExitCode] =
    stringResource
      .use { s => // 2
        IO(s"$s is so cool!").debug
      }
      .as(ExitCode.Success)

  val stringResource: Resource[IO, String] = // 1
    Resource.make(
      IO("> acquiring stringResource").debug *> IO("String")
    )(_ => IO("< releasing stringResource").debug.void)
```

1. Resource[IO,String]をResource.makeで作成し，2つの有効な関数を渡します．この例では、デバッグログを追加して、これらの「ライフサイクル効果」がいつ実行されたかを確認するために、追加のデバッグ・ロギングを行っています。 
2. use メソッドは、作成された String を与えられた関数に供給します。ここでもデバッグログでどのような String 値が渡されたかを確認します。

BasicResourceを実行すると、出力されます。

```shell
[info] [io-compute-3] > acquiring stringResource
[info] [io-compute-3] String is so cool!
[info] [io-compute-3] < releasing stringResource
```

このように、まず価値を獲得し、次にそれを使用し、そして放出する。

ここで重要なのは、Resource自身は何の効果も発揮しないことです。
Resourceは小さなDSL(domain-specific language)であると考えることができる。
そして、useメソッドはそれらの命令を一つのIO値に「コンパイル」します。

また、リリースエフェクトは、使用エフェクトが正常に完了した場合だけでなく、エラーが発生した場合にも実行されます。

```scala
object BasicResourceFailure extends IOApp:

  override def run(args: List[String]): IO[ExitCode] =
    stringResource
      .use(_ => IO.raiseError(new RuntimeException("oh noes!"))) // 1
      .attempt
      .debug
      .as(ExitCode.Success)

  val stringResource: Resource[IO, String] =
    Resource.make(
      IO("> acquiring stringResource").debug *> IO("String")
    )(_ => IO("< releasing stringResource").debug.void)
```

1. 使用効果の内部では、エラーを発生させます。

BasicResourceFailureを実行すると、次のような出力が得られます。
使用効果中に何が起こっても、リソース解放効果は常に実行されます。

```shell
[info] [io-compute-3] > acquiring stringResource
[info] [io-compute-3] < releasing stringResource
[info] [io-compute-3] Left(java.lang.RuntimeException: oh noes!)
```

### 7.1.1. 例 ファイルハンドルのクローズを確認する

開いているファイルから読み込むためのResourceを定義する、より実用的な例です。

```scala
class FileBufferReader private (in: RandomAccessFile) { // 1
  def readBuffer(offset: Long): IO[(Array[Byte], Int)] = // 2
    IO {
      in.seek(offset)
      val buf = new Array[Byte](FileBufferReader.bufferSize)
      val len = in.read(buf)
      (buf, len)
    }
  private def close: IO[Unit] = IO(in.close()) // 3
}
object FileBufferReader {
  val bufferSize = 4096
  def makeResource(fileName: String): Resource[IO, FileBufferReader] = // 4
   Resource.make {
     IO(new FileBufferReader(new RandomAccessFile(fileName, "r")))
   } { res =>
     res.close
   }
}
```

1. Resourceは、FileBufferReaderデータタイプでラップされた、隠れたjava.io.RandomAccessFileを管理します。
2. FileBufferReaderは1つのメソッドのみを公開しています。
3. 非表示の状態をResourceに管理させたいので、closeメソッドを外部の呼び出し元からアクセスできないようにします。
4. IOエフェクトでFileBufferReaderを作成し、Resourceを作成する。Resourceがリリースされたら、その状態を閉じるようにします。

### 7.1.2. 例 バックグラウンドタスクのキャンセル

リソースは、バックグラウンドタスクのライフサイクルを管理するために使用されます。例えば、ある（しばしば非終了的な）効果をフォークして、後で実行する必要がなくなったらそれをキャンセルしたい場合があります。
この場合、Resourceの効果は次のように定義されます。

acquire: タスクを開始し、ファイバーを生成する
release: Fiberをキャンセルします。

この場合、バックグラウンドタスクの寿命は、直接、Resourceの使用効果の実行に対応することになる。

```scala
object ResourceBackgroundTask extends IOApp:

  override def run(args: List[String]): IO[ExitCode] =
    for
      _ <- backgroundTask.use { _ =>
        IO("other work while background task is running").debug *>
          IO.sleep(200.millis) *>
          IO("other work done").debug // 1
      }
      _ <- IO("all done").debug
    yield ExitCode.Success

  val backgroundTask: Resource[IO, Unit] =
    val loop = (IO("looping...").debug *> IO.sleep(100.millis)).foreverM // 2

    Resource.make(
      IO("> forking backgroundTask").debug *> loop.start
    )( // 3
      IO("< canceling backgroundTask").debug.void *> _.cancel // 4
    )
    .void // 5
```

1. バックグラウンドタスクは、私たちの使用効果の間だけ実行されます。
2. バックグラウンドタスク自体は、印刷とスリープを繰り返すループです。foreverMコンビネータを使用します。
   ```scala
   - val loop: IO[Nothing] = step.flatMap(_ => loop)
   + val loop: IO[Nothing] = step.foreverM
   ```
3. acquire の効果は、ファイバーと...
4. ... リリースエフェクトでキャンセルされます。
5. この例では、ResourceのユーザーにFiberへのアクセス権を与えていませんが、これが有用であることは想像できます。

ResourceBackgroundTaskを出力します。

```shell
[info] [io-compute-8] > forking backgroundTask // 1
[info] [io-compute-2] looping...
[info] [io-compute-8] other work while background task is running
[info] [io-compute-2] looping...
[info] [io-compute-7] other work done // 2
[info] [io-compute-2] looping...
[info] [io-compute-7] < canceling backgroundTask // 3
[info] [io-compute-2] all done
```

1. 私たちのエフェクトは、Fiberとしてフォークされています。
2. 使用効果が終了すると...
3. ... ファイバーはキャンセルされます

この「バックグラウンドタスク」のパターンはよくあることなので、Cats Effectは、IO:Backgroundメソッドを定義しています。

```scala
def background: Resource[IO, IO[A]] // 1
```

1. リソース "value "はIO[A]であり、これはバックグラウンドで実行されているエフェクトに参加させるものです。リソースが管理するFiberのjoinメソッドです。

この例からデバッグ効果を取り除いたコードは、次のように書き換えることができます。

```scala
- Resource.make(loop.start)(_.cancel)
+ loop.background
```

バックグランド方式は、手作業による繊維の管理に潜む問題点を解決するものです。
ファイバーは、適切にキャンセルされないと「漏れる」ことがあります。例えば

```scala
def leaky[A, B](ia: IO[A], ib: IO[B]): IO[(A, B)] =
  for {
   fiberA <- ia.start
   fiberB <- ib.start
   a <- fiberA.join // 1
   b <- fiberB.join
  } yield (a, b)
```

1.  iaがエラーを発生させた場合、fiberA.joinは失敗し、fiberBはまだ割り当てられ実行されています。

## 7.2. マネージドステートの構成

個々のResourceの値を定義することは可能ですが、他のResourceからResourceを構築することはできますか？問題ありません、複数の方法で合成できます。

Resourceはファンクタであり、その上にマッピングすることができる。

```scala
val resA: Resource[IO, A] = ???
val resB: Resource[IO, B] = resA.map(makeB)
def makeB(a: A): B = ???
```

リソースはアプリケ-ションであり、2つ以上の値に対してMapNすることができる。

```scala
val resD: Resource[IO, D] =
  (resB, resC).mapN(makeD)
def makeD(b: B, c: C): D = ???
```

Resource はモナドであり，その上を flatMap することができる．もっと便利なのは for-を使うこともできる。

```scala
val resC: Resource[IO, C] =
  for {
    a <- resA
    c <- makeC(a)
  } yield c // 1
def makeC(a: A): Resource[IO, C] = ???
```

1. あるいは resA.flatMap(makeC) と書くこともできます。

BasicResourceの例に別のResourceを追加すると、それらを合成することができます。ライフサイクル効果の実行順序を確認することができます。

```scala
object BasicResourceComposed extends IOApp:

  override def run(args: List[String]): IO[ExitCode] =
    (stringResource, intResource).tupled // 2
      .use {
        case (s, i) => // 2
        IO(s"$s is so cool!").debug *>
          IO(s"$i is also cool!").debug
      }
      .as(ExitCode.Success)

  val stringResource: Resource[IO, String] =
    Resource.make(
      IO("> acquiring stringResource").debug *> IO("String")
    )(_ => IO("< releasing stringResource").debug.void)

  val intResource: Resource[IO, Int] = // 1
    Resource.make(
      IO("> acquiring intResource").debug *> IO(99)
    )(_ => IO("< releasing intResource").debug.void)
```

1. 別のResourceを作成します。今回は管理されたInt値です。
2. stringResource と intResource を tupled で合成し、mapN((s, i) ⇒ (s, i)) とする。Resource[IO, (String.Int)] を生成する。このタプルをuseメソッドに渡される関数で分解することができる。

```shell
[info] [io-compute-8] > acquiring stringResource
[info] [io-compute-8] > acquiring intResource
[info] [io-compute-8] String is so cool!
[info] [io-compute-8] 99 is also cool!
[info] [io-compute-8] < releasing intResource     // 1
[info] [io-compute-8] < releasing stringResource  // 1
```

1. リソースは、取得した順番とは逆に解放されることに注意してください。

### 7.2.1. 並列資源構成

Resourceはモナドなので、様々なコンビネータで合成することができる。Parallel 型クラスのインスタンスを持つので，リソースの管理は逐次ではなく並列に行うことができる

あとは、parプレフィックス付きのコンビネータを使うだけです。例えば，上の
上記のBasicResourceComposedアプリケーションでは、tupledをparTupledに置き換えています。

```scala
- (stringResource, intResource).tupled
+ (stringResource, intResource).parTupled
```

リソースの初期化・クリーンアップはそれぞれのスレッドで行われる。

```shell
[info] [io-compute-4] > acquiring stringResource // 1
[info] [io-compute-5] > acquiring intResource    // 1
[info] [io-compute-7] String is so cool!
[info] [io-compute-7] 99 is also cool!
[info] [io-compute-7] < releasing intResource
[info] [io-compute-7] < releasing stringResource
```

1. リソースの初期化を並列で行う。

## 7.3. 依存関係管理のためのリソース

これまで見てきたコードのほとんどは、並列処理や同時並行処理といったトピックに焦点を当てた短い例です。
これらの例は IOApp を使って書かれていますが、IOApp を使ってより大きなアプリケーションを構築する方法や、効果のセットをより大きなプログラムに合成しようとしたときに発生する可能性のある問題点については、説明していません。

リソースは、値の効果的な割り当てとクリーンアップを完全にカプセル化するため、アプリケーションの依存関係を管理するために使用できます。
IOAppベースのアプリケーションは、3つの異なる関心事に構成されます。

1. 単一の（場合によっては合成された）リソースによって管理される依存関係のライフサイクル。
2. 依存関係を使用するアプリケーション・ロジックは、そのライフサイクルを気にすることなく使用できる
3. アプリケーションのトップレベルでは、依存関係の割り当てが開始されます。ロジックによる使用、そしてそれらをクリーンアップします。

これまでの例で、この構造をすでに使ってきましたが、別のIOAppベースのアプリケーションの例で、懸念事項を明示的に呼び出すことにしましょう。

```scala
object ResourceApp extends IOApp:

  override def run(args: List[String]): IO[ExitCode] =
    resources // 1
      .use { // 3
        case (a, b, c) =>
          applicationLogic(a, b, c) // 2
      }
      .as(ExitCode.Success)

  val resources: Resource[IO, (DependencyA, DependencyB, DependencyC)] = // 1
    (resourceA, resourceB, resourceC).tupled

  val resourceA: Resource[IO, DependencyA] = ???
  val resourceB: Resource[IO, DependencyB] = ???
  val resourceC: Resource[IO, DependencyC] = ???

  def applicationLogic( // 2
    a: DependencyA,
    b: DependencyB,
    c: DependencyC
  ): IO[ExitCode] = ???

  trait DependencyA
  trait DependencyB
  trait DependencyC
```

1. 管理された依存関係のセットを1つのResource値に合成する。
2. アプリケーションロジックは依存関係を使用しますが、依存関係を管理するわけではありません。
3. アプリケーションの冒頭で、管理された依存関係を使用します。アプリケーションロジックに提供します。依存関係は、useブロックのスコープ内にのみ存在します。

## 7.4. まとめ

1. Resourceデータ型は、状態を取得するコードと解除するコードを分離したパターンを捕捉します。Resourceは、他のResource値に直列に、または並列に合成することができる。
2. アプリケーションの依存関係のライフサイクルを表現するために、Resource を使用することができます。そして、IOApp でそれらを使って、依存するコードの実行中にそれらを取得し、それらが解放されるようにします。

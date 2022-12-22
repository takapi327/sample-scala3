---
marp: true
---

# 業務で使えるScala3の機能 (第1回目)

---

# Context Functions

Context Functionsというのはコンテキストパラメーターを持つ関数のことです。

コンテキストパラメーターというのは`?=>`の矢印とはてなの記号を使って表現してるものです。

---

# implicit Paramater

今まで暗黙的な引数を引き回したい時は以下のような実装を行なっていたと思います。

```scala

def future(...)(implicit ec: ExecutionContext): Future[A] = ???

```

---

Scala3だとこの表現は`using`に変わりました。

```scala
def future(...)(using ec: ExecutionContext): Future[A] = ???
```

`using`だと引数名を明示しなくても実装可能になっています。

```scala
def future(...)(using ExecutionContext): Future[A] = ???
```

---

ただ`ExecutionContext`引き回すところなんてたくさんあるし、都度パラメーター書くのってめんどくさくないですか？

---

Context Functionsだったらこう書ける！
暗黙の引数を型で表現できるようになったわけです。

```scala
def future(...): ExecutionContext ?=> Future[A] = ???
```

---

でもこれってパラメーターを都度書く場合と手間はそんなに変わらないのでは？

---

Context Functionsは暗黙の引数を型で表現できるんです！

つまり？

---

型エイリアスを定義することができるんです。

```scala

type Executable[A] = ExecutionContext ?=> Future[A]

def future[A](...): Executable[A] = ???

```

---

特定のメソッドにだけ暗黙の引数増やしたいってなった場合、型エイリアスだと他にも影響でないの？

そこだけ`using`を使った暗黙のパラメーター書かないといけなくなるんじゃないの？

---

こう書かないといけないんだったらちょっと微妙だよね

```scala
def future[A](...)(using String): Executable[A] = ???
```

---

そんなことしなくても大丈夫。
Context Functionsは複数の組み合わせが可能

```scala
def future[A](...): String ?=> Executable[A] = ???
```

---

Lambda式の場合今までは、このIntを暗黙的に書くことはできませんでした。

```scala
val lambda: Int => String = (int: Int) => int.toString
```

コンパイルでエラーになる

```scala
val lambda: Int => String = (implicit int: Int) => int.toString
```

---

Context FunctionsだとLambda式でも使用可能

```scala
val lambda: Int ?=> String = summon[Int].toString
```

---

# Context Functions導入されて嬉しいものは？

例としてPlay FrameworkのActionにContext Functionsが導入されると何が嬉しいでしょうか？

---

今Actionを使った実装ってこういう感じで実装してますよね。

```scala
def get(...) = Action async {
  ...
}
```

---

もし`request`の値を使いたい場合って、こんな感じで明示的に呼び出して使ったり`implicit`をつけて暗黙的に引き回したりしてますよね？

```scala
def get(...) = Action async { request =>
  ...
}

...

def get(...) = Action async { implicit request =>
  ...
}
```

---

なぜそうしないといけないのか？

実装が以下のようになっているからです。

```scala
final def async(block: R[B] => Future[Result]): Action[B] = ???
```

---

もしこれがContext Functionsで実装された場合何が嬉しいでしょうか？

```scala
final def async(block: R[B] ?=> Future[Result]): Action[B] = ???
```

---

暗黙的に引き回したい場合は、暗黙の引数を受け取るものを中で使うだけで良くなる。

```scala
def hoge(using request: Request[AnyContent]) = ???

def get(...) = Action async {
  hoge
  ...
}
```

---

もし暗黙的じゃなく使用したい場合は、`summon`で呼び出すこともできるし、今までと同じように呼び出すこともできます。

```scala
def get(...) = Action async {
  summon[Request[AnyContent]]
  ...
}
```

```scala
def get(...) = Action async { request ?=>
  ...
}
```

---

# 使い分けはどうしたらいいの？

個人的な使い分けですが、暗黙的に受け取った値を変数として明示的に使いたい場合は`using`、暗黙的に引き回すだけであれば`Context Functions`を使えば良いと思ってます。

明確な決まりがあるわけではないので、好きなように実装すれば良いと思います。

# 第11章 結論

最後に、この本の最も重要なコンセプトと、それが「Cats Effect」でどのように表現されているかをまとめておきましょう。

**効果は、私たちのプログラムの推論を助けてくれます。**

効果とは、計算が実行されたときに起こることです。この型によって、プログラムがどのような効果をもたらすか、また、生成する値の型がわかります。同時に、効果のタイプは、私たちが起こしたいことを記述することと、実際にそれを起こすことを分離します。このため、実行するまでの記述をエフェクトに置き換えることができる。

**Cats Effect IOを使えば、安全に副作用を表現することができます。**

IO効果タイプはあらゆる種類の計算をカプセル化し、（最も重要な！）副次的効果もカプセル化します。IO 値を安全に組み合わせて新しい効果を生み出すことができ、それを (unsafe-prefixed メソッドで) 実行したときだけ、何かが起こります。

副作用をとらえる: IO.delay, ...
効果合成: map, mapN, flatMap, ...
効果の実行: unsafeRunSync, ...
Effectベースのアプリケーション: IOApp

**並列処理によって、独立した作業をより効率的に行うことができます。**

並列処理は、複数のスレッドに作業をスケジューリングすることで実装されており、n 個のスレッドがある場合、最大 n 個の効果が並列に実行されます。より馴染みのある（しかし順次実行される）メソッドの並列バージョンは par がプレフィックスとして付きます： parMapN, parTraverse, などです．並列実行中にエラーが発生した場合、残りのエフェクトはキャンセルされます。

並列実行: parMap, parTraverse, parSequence, parTupled

**同時実行により、どのようにスケジュールされるかを明示的に指定することなく、特定の実行動作を宣言することができる**

Cats Effect を使用すると、同時実行中のエフェクトを開始し、返された Fiber を使用して、エフェクトが完了するまで待機したり（join）、停止するように要求したり（cancel）することができます。より高いレベルでは、2つのエフェクトを同時に「レース」することで、どちらが先に終了したかを知った上で、その後に行動することができます。

効果をあげる: start
同時通訳: join, cancel
Effectの競争: IO.race, IO.racePair

**コンテキストは、「どこで」計算が実行されるかという計算資源のプールを表します。**

列効果や並行効果を実行すると、現在のコンテキストで利用可能なスレッドにスケジューリングされます。しかし、これらのコンテキストを経由して、エフェクトの実行を制御することもできます。コンテクスト内では、非同期境界が後続の実行を再スケジュールし、同時に実行されている他のエフェクトが進行する機会を得られるようにします。同時に、複数のコンテキストを持つことで、実行中のエフェクトを互いに隔離しておくことができる。Cats Effect は、ノンブロッキングで CPU に依存する作業をあるコンテキストで行い、ブロッキングで I/O に依存する作業を別のコンテキストで行うという、一般的なアプリケーションパターンをサポートします。

降伏実行／リスケジュール: (CE2) IO.shift, (CE3) IO.cede, evalOn
デクレア ブロッキング効果: (CE2) Blocker, (CE3) IO.blocking

**非同期は、計算をその結果を処理するコード（継続）から切り離すことを可能にします。**

計算の結果を待つ代わりに、その結果の処理を遅延させることで、現在のエフェクトがブロックされることなく継続できるようになります。Cats Effectでは、非同期計算からエフェクトを構築することができ、Futureなどの非同期型と統合することができる。

非同期を統合する: IO.async、IO.fromFuture

**資源は、状態の獲得と解放をその使用から分離する。**

Resourceを宣言することで状態のライフサイクルを明示的に管理し、複数のResourceを組み合わせて構成することができる。リソースは直列にも並列にも構成でき、構成されたリソースは構成するリソースが適切な順序で取得され、解放されることを保証する。リソースは、ライフサイクルを管理する必要がある依存関係をモデル化するための自然なデータ型である。

Resourceの作成: Resource.make
Resourceの合成: map, mapN, flatMap, etc., plus par-prefixed versions
Resourceの使用: use

**効果を検証するには、その実行が必要です。**

このような副作用は、実際に「ミサイルを発射する」ときに問題になることがありますが、そのような副作用が起こることだけを主張したい場合もあります。この危険性を軽減するために、効果的なメソッドを抽象化するインターフェースを作成し、テスト時に偽の実装を提供できるようにします。Cats EffectのTestContextは、エフェクトのスケジューリングのレベルで、実際の実行システムを、プログラマが制御できるものに置き換えることができる。

テストにおける制御効果スケジューリング: TestContext

**並行効果を調整するためには、同期制約を表現する（そしてそれを尊重する）抽象化が必要です。**

例えば、Refを使って「ミュータブル」な状態を共有することができ、更新が適切に同期されることを保証します。また、別のエフェクトが値を生成するまで、あるエフェクトを（意味的に）ブロックしたい場合は、Deferredを使うことができます。この2つのプリミティブを使うことで、並行ステートマシンのような技術を使ってより複雑な並行処理を構築することができます。

アトミックに共有状態を更新: Ref
ブロッキング同期: Deferred

## 11.1. 次のステップ

この本は、より大きなカリキュラムとコミュニティの一部です。次に何を探求するか、いくつかのステップを提案します。

**質問、アドバイス、Cats Effect コミュニティのメンバーとの出会い**

Join the Cats Effect chatroom.[32]

**Cats Effectの上に、より強力な抽象化機能を構築し、利用する。**

例えば、fs2[33]は洗練されたフロー制御構造を構築できる強力なライブラリ、doobie[34]はJDBCデータベース操作をラップし、http4s[35]はHTTPスタック全体である。Cats Effectを利用した他のライブラリやプロジェクトは、Cats Effectのメインサイトにリストアップされています。[36]

**関数型プログラミングの概念とパターンについて詳しく知る**

Noel WelshとDave GurnellによるScala with Cats [3]をお読みください。

**関数型プログラミングによるアプリケーションのアーキテクトについて学びます。**

Scalaの実用的なFPを読む。Gabriel Volpe著「A hands-on approach」[4]。

**機能的なドメインモデルを構築する方法と、構成可能な抽象化を構築する方法を学びます。**

デバシシュ・ゴッシュ著「Functional and Reactive Domain Modeling」[5]を読む。

** 参考文献

- [1] Dave Gurnell and Noel Welsh. https://creativescala.com
- [2] Noel Welsh and Dave Gurnell. Essential Scala. https://underscore.io/books/essential-scala
- [3] Noel Welsh and Dave Gurnell. Scala with Cats. https://www.scalawithcats.com
- [4] Gabriel Volpe. Practical FP in Scala: A hands-on approach. https://leanpub.com/pfp-scala
- [5] Debasish Ghosh. Functional and Reactive Domain Modeling. https://www.manning.com/books/functional-and-reactive-domain-modeling
- [6] Allen B. Downey. The Little Book of Semaphores. https://greenteapress.com/ wp/semaphores

[32] https://gitter.im/typelevel/cats-effect [33] https://fs2.io
[34] http://tpolecat.github.io/doobie/
[35] http://http4s.org/
[36] https://typelevel.org/cats-effect/

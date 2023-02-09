# カラム定義

- VISIBLE, INVISIBLE

カラムの可視性を指定します。 どちらのキーワードも存在しない場合、デフォルトは VISIBLE です。 テーブルには、少なくとも 1 つの表示可能なカラムが必要です。 すべてのカラムを非表示にしようとすると、エラーが発生します。 詳細は、セクション13.1.20.10「非表示カラム」を参照してください。

VISIBLE および INVISIBLE キーワードは、MySQL 8.0.23 の時点で使用できます。 MySQL 8.0.23 より前は、すべてのカラムが表示されます。

特段必要ではなさそうなため実装は行わない。

- AUTO_INCREMENT

整数または浮動小数点のカラムには、追加の属性 AUTO_INCREMENT を指定できます。 

テーブルごとに存在できる AUTO_INCREMENT カラムは 1 つだけです。このカラムはインデックス付きである必要があり、DEFAULT 値を割り当てることはできません。 AUTO_INCREMENT カラムは、正の値だけが含まれている場合にのみ正しく機能します。 負の数を挿入すると、非常に大きな正の数を挿入したと見なされます。 これは、数字が正から負に「ラップする」ときの精度の問題を回避すると同時に、0 を含む AUTO_INCREMENT カラムを誤って取得してしまわないようにするために行われます。

```sql
CREATE TABLE `auto_inc_test` (
  `id` BIGINT(64) NOT NULL AUTO_INCREMENT PRIMARY KEY
);
```

- PRIMARY KEY

すべてのキーカラムを NOT NULL として定義する必要がある一意のインデックス。 それらが NOT NULL として明示的に宣言されていない場合、MySQL は、それらを暗黙的に (かつ警告なしで) そのように宣言します。 テーブルに存在できる PRIMARY KEY は 1 つだけです。 PRIMARY KEY の名前は、常に PRIMARY です。そのため、これをその他のどの種類のインデックスの名前としても使用できません。

- REFERENCES

https://dev.mysql.com/doc/refman/5.6/ja/example-foreign-keys.html

2 つのテーブルを結合するだけの場合は、外部キー制約は必要ありません。InnoDB 以外のストレージエンジンの場合、カラムを定義するときに REFERENCES tbl_name(col_name) 句を使用できます。これは実際の効果はありませんが、現在定義しようとしているカラムが別のテーブルのカラムを参照する予定であるという自分のメモまたはコメントとして役立ちます。この構文を使用するときは、次の点を理解しておくことが非常に重要です。

- MySQL は、col_name が実際に tbl_name に存在するか (また、その tbl_name 自体が存在するか) を確認するためのどのような CHECK も実行しません。
- MySQL は、tbl_name に対してどのようなアクションも実行しません。たとえば、定義しようとしているテーブルの行に実行されたアクションに対応して行を削除することなどはありません。つまり、この構文にはどのような ON DELETE 動作や ON UPDATE 動作もありません。(REFERENCES 句の一部として ON DELETE 句や ON UPDATE 句を記述することはできますが、これらも無視されます。)
- この構文はカラムを作成します。どのようなインデックスやキーも作成しません。

- CONSTRAINT CHECK

MySQL バージョン8系からのみ使用できる。一旦実装は保留にしておく。

## メモ

- SchemaSpyへの追加情報はXMLを作成して行う

https://gift-tech.co.jp/articles/schemaspy-meta-yaml/

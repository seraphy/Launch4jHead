# Launch4jのHEADのカスタマイズ

## 概要

Launch4jのgui用headをカスタマイズして、以下のような挙動にする。

- --l4j-debug の指定がある場合は、コンソール画面も開く
- ???.l4j.iniファイルがない場合は、 ???.ini ファイルを使用する。
  - (通常、javaは実行可能jarと同じフォルダ上にiniファイルを置く習慣はないので、???.ini は使われていないと思われる。)
- オプション引数(args)も変数展開する
- 環境変数 %FIND_ANCESTOR:???% で %EXEDIR%上で???にマッチするファイルまたはディレクトリを検索して、マッチしたフルパスを返す。存在しない場合は更に親フォルダを検索する。発見できなければ空を返す。


## ビルド方法

### Dev-C++ 5.0.2

元となるソースは、[Launch4j](http://launch4j.sourceforge.net/)のver3.12である。

Launch4j 3.12では、MinGWのbinutils 2.22を使用しており、このバージョンから、おそらく、gccは32ビット版の4.6.2あたりを使っていると思われる。


そのため、これに近いバージョンのコンパイル用として、[Dev-C++ 5.0.2](https://sourceforge.net/projects/orwelldevcpp/files/Setup%20Releases/) を使用している。

(完全には一致していないので、*.o, *.a は、Dev-C++ 5.0.2 でビルドに使ったものに置き換える必要がある。)

プロジェクトファイルは ```src/Launch4jStub/head_src/guihead/guihead.dev``` にあり、Dev-C++ で開くことができる。

(なお、```head.c``` ソースは、```consolehead``` のプロジェクトと共通である。)


### サンプルのビルド

このカスタマイズされたヘッダを使用した実行可能jarをexeファイル化するサンプルは maven でビルドできる。

```shell
mvn package
```

これで、targetディレクトリ上に このヘッダを使ったLaunch4jで生成された実行ファイル ```launch4jhead.exe``` が得られる。

このサンプルプログラムは、Swingで記述されたGUIアプリであり、システムプロパティと環境変数の一覧を表示する。

END.


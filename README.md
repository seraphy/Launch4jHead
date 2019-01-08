# Launch4jのカスタムGUI HEAD

本カスタムヘッドでは、Launch4jの標準のjava起動部に対して、以下のような機能を追加するものである。

- JREの探索順序を拡張する
  - **\*.cfgファイルのSETTINGSセクションにあるJAVA_HOMEキー**
  - バンドルJRE
  - レジストリによるJRE/JDK探索
  - いずれも見つからなかった場合は、**フォルダ選択ダイアログを表示しユーザーにJAVA_HOMEを選択**してもらう
    - ユーザーの選択したJAVA_HOMEはjava.exe, javaw.exeの実在の確認と、x64, x86の条件がマッチするかまでは確認する。
      - ただし、(現時点では)javaのバージョンが妥当であるかは確認しない。
    - 選択されたJAVA_HOMEでjavaがエラーなく起動できた場合は、*.cfgファイルのSETTINGSセクションのJAVA_HOMEに記録される。
    - *.cfgファイルずがなければ作成される。(作成できない場合はエラーにせず、単に無視する。)
- Launch4jの設定でビルド時に指定する**固定のコマンドラインも変数展開する**
  - ユーザーが起動時に指定する引数は(従来どおり)変数展開されない。
- \*.l4j.iniファイルがみつからない場合は、**\*.iniファイル**を使う。
- \*.cfgファイルの**ENVIRONMENTS**セクションにあるキーと値で実行時に環境変数が設定される。
  - ビルド時に指定した固定の環境変数が設定されたあと、\*.cfgファイルの環境変数が設定される。
  - 環境変数の設定はJVMオプションの設定より先に行われるので、JVMオプションで環境変数展開につかえる。
- \*.cfgファイルの**JVM_OPTIONS**セクションにある値でJVMパラメータを指定できる。
  - iniファイルの形式はKEY=VALUEでなければならないので、KEYはなんでも良いので適当につけておく。
  - ビルド時に指定した固定のJVMオプションが設定されたあと、\*.cfgのJVMオプションが設定され、最後に\*.l4j.ini(または\*.ini)が設定される。
- 以下の特殊変数をサポートする。(太字のものが追加されたもの。それ以外は標準のものである)
  - **FIND_ANCESTOR** EXEDIRから親に向かって特定の名前にマッチするファイルまたはフォルダを検索する
  - **JRE_ARCH** 実行するJAVAのx86, x64のタイプを示す
  - EXEDIR 実行ファイルのあるディレクトリ
  - EXEFILE 実行ファイルのパス
  - PWD 設定された現在ディレクトリ
  - OLDPWD 起動時のカレントディレクトリ
  - JREHOMEDIR JREのホームディレクトリ
  - HKEYで始まるレジストリキー名 レジストリの値
  - それ以外は普通の環境変数を展開する

## CFGファイルについて

cfgファイルは、exeの拡張子をcfgに変えたファイル名をもつテキストファイルであり、INIファイルと同じフォーマットである。

このファイルはなくてもかまわない。

|セクション  |キー          |内容                              |
|:-----------|:-------------|:---------------------------------|
|SETTINGS    |JAVA_HOME     |起動に使うJAVA_HOMEを指定する     |
|            |SAVEJAVAHOME  |0または1。0でJAVA_HOMEを保存しない|
|ENVIRONMENTS|任意の環境変数|任意の環境変数の値                |
|JVM_OPTIONS |適当な名前    |JVMオプション                     |

## 追加された特殊変数について

### FIND_ANCESTOR

```FIND_ANCESTOR```は、指定した名前をもつファイルまたはディレクトリを```EXEDIR```からルートに向かって探索し、
みつかったフルパスを返すものである。

みつからない場合は空となる。

探索するファイル名はコロンの後で指定し、フォルダ区切りを含めて良い。

```%FIND_ANCESTOR:探索するファイル名%``` のように指定する。

フォルダ区切りを含めて良いので、

```
ABC_HOME=%FIND_ANCESTOR:A\B\C%
```
のように指定することができる。

この場合、EXEDIRがQ:\R\Sという場所にあった場合、

1. Q:\R\S\A\B\C
2. Q:\S\A\B\C
3. Q:\A\B\C

のように探索される。

### JRE_ARCH

```JRE_ARCH```は、実行するjava.exe(またはjavaw.exe)が32ビットまたは64ビットのどちらかであるかを表す。

```%JRE_ARCH:i386,amd64%``` のようにx86, x64として表現する文字列を指定することができる。

この場合、x86の場合は「i386」、x64の場合は「amd64」という文字列が返される。

この文字列は省略することができ、

```%JRE_ARCH%```とした場合は、「x86」「x64」のいずれかの文字列となる。

## このカスタムヘッダを使ったexeの生成方法 (サンプルのビルド方法)

このカスタマイズされたヘッダを使用した実行可能jarをexeファイル化するサンプルは maven でビルドできる。

```shell
mvn package
```

これで、targetディレクトリ上に このヘッダを使ったLaunch4jで生成された実行ファイル ```launch4jhead.exe``` が得られる。

なお、使用しているMavenプラグインは
```xml
<!-- https://mvnrepository.com/artifact/com.akathist.maven.plugins.launch4j/launch4j-maven-plugin -->
<dependency>
    <groupId>com.akathist.maven.plugins.launch4j</groupId>
    <artifactId>launch4j-maven-plugin</artifactId>
    <version>1.7.25</version>
</dependency>
```
である。 (注意: プラグインのバージョンは**1.7.25以降でないとカスタムヘッダがつかえない**)

このサンプルプログラムは、Swingで記述されたGUIアプリであり、システムプロパティと環境変数の一覧を表示する。

(なお、一応、Java8, Java11のいずれで実行しても、HiDpi対応しており、スクリーンスケールにあわせて画面サイズを設定するようになっている。)


## カスタムヘッダのビルド方法

### Dev-C++ 5.0.2 (MinGW GCC 4.6.2 32Bit)の使用

本カスタムヘッダの元となるソースは、[Launch4j](http://launch4j.sourceforge.net/)のver3.12である。

Launch4j 3.12では、MinGWのbinutils 2.22を使用しており、このバージョンから、おそらく、gccは32ビット版の4.6.2あたりを使っていると思われる。


そのため、これに近いバージョンのコンパイル用として、[Dev-C++ 5.0.0.9](https://sourceforge.net/projects/orwelldevcpp/files/Portable%20Releases/) を使用している。

(完全には一致していないので、*.o, *.a は、Dev-C++ 5.0.0.9 でビルドに使ったものに置き換える必要がある。)

プロジェクトファイルは ```src/Launch4jStub/head_src/guihead/guihead.dev``` にあり、Dev-C++ で開くことができる。

(なお、```head.c``` ソースは、```consolehead``` のプロジェクトと共通である。)

END.


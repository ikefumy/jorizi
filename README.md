# ソフトウェア制作

## Server.javaについて
Clientが発生したことを検知したらクライアントに色々割り当てた後,クライアントにplayer_numberを送信しています。

## Client.javaについて
コンストラクタにおいて、
`FallPieceThread`ではTetrisの関数を呼び出すことで毎秒ミノが降下するようにしています。
`MesgRecvThread`ではServerからのメッセージを受信、処理しています。また、`palyer number`の偶奇に応じて`wasde`で操作するか、`ijklo`で操作するかを死分けています。

## 操作方法について
1. Server.javaを動かします。
2. Client.javaを二つのターミナルで動かします。
3. タイトルがServerとなっているタブ上で操作します。
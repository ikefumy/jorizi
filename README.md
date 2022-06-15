# ソフトウェア制作

## Server.javaについて
Clientが発生したことを検知したらクライアントに色々割り当てた後,クライアントにplayer_numberを送信しています。

## Client.javaについて
コンストラクタにおいて、
`FallPieceThread`ではTetrisの関数を呼び出すことで毎秒ミノが降下するようにしています。
`MesgRecvThread`ではServerからのメッセージを受信、処理しています。また、`palyer number`の偶奇に応じて`wasde`で操作するか、`ijklo`で操作するかを死分けています。
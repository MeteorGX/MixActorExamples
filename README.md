# Mix Actor Examples

开发的主要思想是 `Bean Is Actor`, 把 `Spring` 的对象声明挂载在 `JVM` 内存等待客户端来调用; 
一般简单的 `H5WebSocket` 游戏服务器可以直接引入挂载开发, 也可以作为简单长连接聊天室和音频传输功能.

`MixActor` 功能示例, 用于简单的 `Websocket` 功能业务, 包含示例内容:
* `Echo`: 简单的 `Websocket` 回显服务器
* `Actors`: 业务功能 `Actor` 挂载


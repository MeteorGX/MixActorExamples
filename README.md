# Mix Actor Examples #

开发的主要思想是 `Bean Is Actor`, 把 `Spring` 的对象声明挂载在 `JVM` 内存等待客户端来调用; 
一般简单的 `H5WebSocket` 游戏服务器可以直接引入挂载开发, 也可以作为简单长连接聊天室和音频传输功能.

`MixActor` 功能示例, 用于简单的 `Websocket` 功能业务, 包含示例内容:
* `Echo`: 简单的 `Websocket` 回显服务器, 数据传输内容.
* `Actors`: 业务功能 `Actor` 挂载, 客户端转发命令到服务端的 `Bean`.
* `Mission`: 成就系统功能, 最常用的任务系统, 游戏系统当中必须的功能.


### echo(网络库) ###

针对 `H5` 页游服务端基本都是特性都是实时性要求不高, 基本集中客户端运算或者开房式的三消塔防之类的, 
这种游戏对于多人同屏/多人社交要求要求不高, 所以对于性能要求上面可以适当放宽让游戏服务挂载在 `JVM` 执行.

> 后续扩展可以抛弃 `SpringBoot-websocket` 模块采用 `Netty` 挂载即可, 基本上思路都是一致.

采用 `SpringBoot-websocket` 的好处主要是工具集相对完整可以直接断点调试, 但是坏处就是太过重型了导致比较冗余.

这里基于 `Java` 本身特性包装事件库来做定时心跳转发, 事件库目前仅作为 `所有者+事件名` 来合并作为单条事件, 
而在 `echo` 项目之中仅仅作为心跳保活来处理.


### actors(业务代理) ###

可以参考 `Skynet` 的开发思想, 这里只是简单把游戏业务挂载到在服务器, 等待拦截客户端发起调用.

> 这里主要思想是 `Bean Is Actor`, 把 `Java` 的 `Bean` 概念抽取成 `Actor` 提供客户端服务.

```java
// 这种方式主要依托 Java 的注解功能, 从而提取出 @ActorController + @Component 做快速开发

/**
 * 自定义的 Actor 服务
 * 对于 @Component 来说, 会让 SpringContext 将其挂载全局唯一对象
 * 对于 @ActorController 这是自己封装的 Actor 注解声明, 通过声明所有者来让其调用
 * 这里声明之后这类启动将会是唯一对象
 * @author MeteorCat
 */
@Component
@ActorController(ActorWebSocketApplication.class)
public class HelloThisActor{

    /**
     * 网关对象, @ActorRuntime 自定义注解, 把启动网关传递过来让其内部可用
     */
    @ActorRuntime
    ActorWebSocketApplication gateway;


    /**
     * 暴露给客户端的接口功能
     * @param session 会话对象
     * @param value 响应值
     * @param data 响应数据
     */
    @ActorMapping(200)
    public void timestamp(WebSocketSession session, Integer value, JsonNode data){
        // do something
    }
}
```

这里通过 `ActorMapping` 就能暴露出 `value=200` 的映射 `Bean` 入口方法.

### mission(读表任务) ###

游戏启动的时候都会去加载 `excel` 的策划表从而读取系统配置, 这种是游戏读表方面的常见配置; 
通过 `python` 简单处理读取表之后让 `Spring` 启动运行装载这些配置, 从而客户端可以检索任务是否完成.

一般来说任务记录表有几种类型:
* `单次签到(checkout)`: 单一性单天触发任务, 最简单的形式.
* `数值进度(progress)`: 最常见的游戏当中当天消费游戏币( `游戏币:1/1000` )形式, 这种需要监听所有消费时间之后触发.
* `复合进度(multiple)`: 复杂的混合条件, 比如( `签到:1/1, 游戏币:1/1000, NPC交谈:1/1` ), 结合进度和触发的多重进度.

其他还有年月日等签到扩展, 不过基本上都是以上扩展衍生.









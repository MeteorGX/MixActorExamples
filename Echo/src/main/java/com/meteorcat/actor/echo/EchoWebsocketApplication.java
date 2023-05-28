package com.meteorcat.actor.echo;

import com.meteorcat.mix.core.event.Event;
import com.meteorcat.mix.core.event.EventMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket服务
 * @author MeteorCat
 */
@Component
public class EchoWebsocketApplication extends TextWebSocketHandler {

    /**
     * 日志句柄
     */
    Logger logger = LoggerFactory.getLogger(EchoWebsocketApplication.class);


    /**
     * 事件池
     */
    @Autowired
    EventMonitor<WebSocketSession,String> eventMonitor;


    /**
     * 请求状态: Established
     * @param session 会话对象
     * @throws Exception 异常
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        if(logger.isInfoEnabled()){
            logger.info("[Echo] Established = {}",session);
        }
    }

    /**
     * 关闭状态: Close
     * @param session 会话对象
     * @param status 关闭状态
     * @throws Exception 异常
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        if(logger.isInfoEnabled()){
            logger.info("[Echo] Close({}) = {}",status,session);
        }

        // 删除监听的事件
        eventMonitor.remove(session);
    }

    /**
     * 传输错误异常
     * @param session 会话对象
     * @param exception 异常信息
     * @throws Exception 异常
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        super.handleTransportError(session, exception);
        if(logger.isErrorEnabled()){
            logger.error("[Echo] Error = {} | {}",exception.getMessage(),session);
        }

        // 删除监听的事件
        eventMonitor.remove(session);
    }


    /**
     * 文本消息传递
     * @param session 会话对象
     * @param message 传输内容
     * @throws Exception 异常
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        // 追加保活事件, 每15s推送心跳保活
        final String heartbeatEventName = "heartbeat";
        final long heartbeatSecond = 15L;
        if(!eventMonitor.containsKey(session,heartbeatEventName)){
            eventMonitor.putEvent(session,heartbeatEventName,new Event(()->{
                // 连接关闭, 删除事件
                if(!session.isOpen()){
                    eventMonitor.remove(session,heartbeatEventName);
                    return ;
                }

                // 推送定时心跳保活
                try {
                    long timestamp = System.currentTimeMillis();
                    session.sendMessage(new TextMessage(Long.toString(timestamp)));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }));

            // 定时执行
            eventMonitor.scheduleAtFixedRate(session,heartbeatEventName,heartbeatSecond,heartbeatSecond, TimeUnit.SECONDS);
        }


        // 短消息不回应
        if(message.getPayloadLength() <= 0 ){
            return ;
        }

        // 接收到客户端请求数据响应返回
        session.sendMessage(message);
    }
}

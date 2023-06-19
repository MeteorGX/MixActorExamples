package com.meteorcat.mission;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * WebSocket服务
 * @author MeteorCat
 */
@Component
public class MissionWebSocketApplication extends TextWebSocketHandler {

    /**
     * 日志句柄
     */
    Logger logger = LoggerFactory.getLogger(MissionWebSocketApplication.class);



    /**
     * 要求客户端请求数据格式: { xxx:int, yyy:{ ... } }
     * 这里设定 xxx 请求的映射id
     */
    @Value("${mix.mission.request.value.name:value}")
    private String requestValueName;


    /**
     * 要求客户端请求数据格式: { xxx:int, yyy:{ ... } }
     * 这里设定 xxx 请求的数据节点
     */
    @Value("${mix.mission.request.data.name:data}")
    private String requestDataName;


    /**
     * JSON解析器
     */
    ObjectMapper mapper = new ObjectMapper();



    /**
     * Established
     *
     * @param session 请求会话
     * @throws Exception 异常
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        if (logger.isInfoEnabled()) {
            logger.info("[Mission] Established = {}", session);
        }
    }


    /**
     * Close
     *
     * @param session 请求会话
     * @param status  关闭状态
     * @throws Exception 异常
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        if (logger.isInfoEnabled()) {
            logger.info("[Mission] Close({}) = {}", status, session);
        }
    }




    /**
     * 传输错误异常
     *
     * @param session   会话对象
     * @param exception 异常信息
     * @throws Exception 异常
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        super.handleTransportError(session, exception);
        if (logger.isErrorEnabled()) {
            logger.error("[Mission] Error = {} | {}", exception.getMessage(), session);
        }
    }


    /**
     * 文本消息传递
     *
     * @param session 会话对象
     * @param message 传输内容
     * @throws Exception 异常
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 消息长度不足
        if (message.getPayloadLength() <= 0) {
            return;
        }

        // 解析JSON


    }



}

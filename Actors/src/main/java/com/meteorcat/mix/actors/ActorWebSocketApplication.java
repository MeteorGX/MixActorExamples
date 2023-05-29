package com.meteorcat.mix.actors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meteorcat.mix.actors.config.WebsocketConfig;
import com.meteorcat.mix.core.actor.ActorSearcher;
import com.meteorcat.mix.core.actor.ActorTuple;
import com.meteorcat.mix.core.utils.JsonNodeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 挂载Actor的服务对象
 *
 * @author MeteorCat
 */
@Component
public class ActorWebSocketApplication extends TextWebSocketHandler implements ApplicationContextAware {

    /**
     * 日志句柄
     */
    Logger logger = LoggerFactory.getLogger(ActorWebSocketApplication.class);


    /**
     * 要求客户端请求数据格式: { xxx:int, yyy:{ ... } }
     * 这里设定 xxx 请求的映射id
     */
    @Value("${mix.actors.request.value.name:value}")
    private String requestValueName;


    /**
     * 要求客户端请求数据格式: { xxx:int, yyy:{ ... } }
     * 这里设定 xxx 请求的数据节点
     */
    @Value("${mix.actors.request.data.name:data}")
    private String requestDataName;


    /**
     * JSON解析器
     */
    ObjectMapper mapper = new ObjectMapper();


    /**
     * 检索项目当中注册 ActorBean
     */
    @Nullable
    Map<Integer, ActorTuple> actors;


    /**
     * SpringApplicationContext注册
     *
     * @param applicationContext the ApplicationContext object to be used by this object
     * @throws BeansException Bean异常
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // 检索出项目当中 Bean 对象
        try {
            actors = ActorSearcher.searchMapping(applicationContext, ActorWebSocketApplication.class, this);
        } catch (IllegalAccessException e) {
            if (logger.isErrorEnabled()) {
                logger.error("[Actors] Failed by Search Actor = {}", e.getMessage());
            }
        }

        // 打印检索的 Actor Bean 对象
        if (actors != null && logger.isInfoEnabled()) {
            for (Map.Entry<Integer, ActorTuple> actor : actors.entrySet()) {
                logger.info("[Actors] Search Actor({}) = {}::{}",
                        actor.getKey(),
                        actor.getValue().getInstance().getClass().getName(),
                        actor.getValue().getMethod().getName()
                );
            }
        }
    }


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
            logger.info("[Actors] Established = {}", session);
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
            logger.info("[Actors] Close({}) = {}", status, session);
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
            logger.error("[Actors] Error = {} | {}", exception.getMessage(), session);
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
        // 确认 Actor 是否非 null 可用
        if (actors == null) {
            session.close(CloseStatus.SERVER_ERROR);
            return;
        }


        // 消息长度不足
        if (message.getPayloadLength() <= 0) {
            return;
        }

        // 获取数据内容尝试解析成JSON
        String payload = message.getPayload();
        JsonNode json = null;
        try {
            json = mapper.readTree(payload);
        } catch (JsonProcessingException e) {
            if (logger.isErrorEnabled()) {
                logger.error("[Actors] Failed By Parse Json = {}", e.getMessage());
            }
        }

        // 确认是否JSON数据
        if (!session.isOpen() || json == null) {
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        // 解析内部字段是否存在
        Optional<Integer> valueOptional = JsonNodeUtil.isInteger(json, requestValueName);
        Optional<JsonNode> dataOptional = JsonNodeUtil.isObject(json, requestDataName);
        if (valueOptional.isEmpty() || dataOptional.isEmpty()) {
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        // 检出JSON数据转发调用
        forwardActor(session, valueOptional.get(), dataOptional.get());
    }


    /**
     * 转发调用 Actor
     *
     * @param session 会话对象
     * @param value   转发值
     * @param data    转发数据
     */
    public void forwardActor(WebSocketSession session, Integer value, JsonNode data) {
        if (actors == null) {
            return;
        }

        // 检索出 Actor
        ActorTuple tuple = actors.get(value);
        if (Objects.isNull(tuple)) {
            return;
        }

        // 转发调用
        try {
            ReflectionUtils.invokeMethod(tuple.getMethod(), tuple.getInstance(), session, value, data);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("[Actors] Failed By Redirect Actor = {}", e.getMessage());
            }
        }
    }


    /**
     * 响应给客户端数据
     *
     * @param session 会话对象
     * @param value   响应值
     * @param data    响应数据
     */
    public void response(WebSocketSession session, Integer value, Map<String, Object> data) {
        // 构建响应体
        Map<String, Object> response = new HashMap<>(2) {{
            put(requestValueName, value);
            put(requestDataName, data);
        }};

        // 转为JSON数据
        Optional<String> responseOptional = JsonNodeUtil.toMapStr(mapper, response);
        if (responseOptional.isEmpty()) {
            return;
        }

        // 推送数据响应体
        try {
            session.sendMessage(new TextMessage(responseOptional.get()));
        } catch (IOException e) {
            if (logger.isErrorEnabled()) {
                logger.error("[Actors]({}) Failed By Response Frame = {}", value, e.getMessage());
            }
        }
    }
}

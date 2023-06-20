package com.meteorcat.mission;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meteorcat.mix.core.actor.ActorSearcher;
import com.meteorcat.mix.core.actor.ActorTuple;
import com.meteorcat.mix.core.event.EventMonitor;
import com.meteorcat.mix.core.utils.JsonNodeExtends;
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
import java.util.Optional;
import java.util.Set;

/**
 * WebSocket服务
 * @author MeteorCat
 */
@Component
public class MissionWebSocketApplication extends TextWebSocketHandler implements ApplicationContextAware {

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
     * 事件管理器
     */
    @Autowired
    EventMonitor<WebSocketSession,String> eventMonitor;


    /**
     * 检索项目当中注册 ActorBean
     */
    @Nullable
    Map<Integer, ActorTuple> actors;


    /**
     * 加载Spring对象
     * @param applicationContext the ApplicationContext object to be used by this object
     * @throws BeansException Bean错误
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // 检索出注册的Bean业务
        try {
            actors = ActorSearcher.searchMapping(applicationContext, MissionWebSocketApplication.class, this);
        } catch (IllegalAccessException e) {
            if (logger.isErrorEnabled()) {
                logger.error("[Mission] Failed by Search Actor = {}", e.getMessage());
            }
        }

        // 打印检索的 Actor Bean 对象
        if (actors != null && logger.isInfoEnabled()) {
            for (Map.Entry<Integer, ActorTuple> actor : actors.entrySet()) {
                logger.info("[Mission] Search Actor({}) = {}::{}",
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
        Optional<JsonNode> rootNodeOpt = JsonNodeExtends.isNode(mapper,message.getPayload());
        if(rootNodeOpt.isEmpty()){
            session.close(CloseStatus.BAD_DATA);
            return ;
        }


        // 确认指令|数据字段存在
        JsonNode rootNode = rootNodeOpt.get();
        Optional<Integer> typeNodeOpt = JsonNodeExtends.isInteger(rootNode,requestValueName);
        Optional<JsonNode> dataNodeOpt = JsonNodeExtends.isObject(rootNode,requestDataName);
        if(typeNodeOpt.isEmpty() || dataNodeOpt.isEmpty()){
            session.close(CloseStatus.BAD_DATA);
            return ;
        }

        // 转发事件
        Integer type = typeNodeOpt.get();
        if(actors != null && actors.containsKey(type)){
            ActorTuple tuple = actors.get(type);
            ReflectionUtils.invokeMethod(tuple.getMethod(),tuple.getInstance(),session,type,dataNodeOpt.get());
        }
    }


    /**
     * 推送消息返回客户端
     * @param session 客户端句柄
     * @param type 消息类型
     * @param data 消息数据
     */
    public void send(WebSocketSession session,Integer type,Map<String,Object> data){
        if(!session.isOpen()){
            return ;
        }

        try{
            // 数据格式
            Map<String,Object> response = new HashMap<>(2){{
                put(requestValueName,type);
                put(requestDataName,data);
            }};

            // 转化成JSON
            Optional<String> json = JsonNodeExtends.isMapStr(response);
            if (json.isPresent()){
                //推送数据
                session.sendMessage(new TextMessage(json.get()));
            }
        }catch (IOException e){
            logger.error(e.getMessage());
        }
    }

}

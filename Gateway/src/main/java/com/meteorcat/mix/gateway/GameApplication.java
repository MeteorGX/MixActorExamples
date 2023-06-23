package com.meteorcat.mix.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meteorcat.mix.core.actor.ActorSearcher;
import com.meteorcat.mix.core.actor.ActorTuple;
import com.meteorcat.mix.core.utils.JsonNodeExtends;
import com.meteorcat.mix.gateway.service.PlayerOnlineService;
import lombok.extern.slf4j.Slf4j;
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

/**
 * 游戏服务器, 采用文本JSON协议传输, 正式游戏推荐采用序列化类似GoogleProtobuf来节约传输时候的数据量
 * @author MeteorCat
 */
@Slf4j
@Component
public class GameApplication extends TextWebSocketHandler implements ApplicationContextAware {

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
     * 检索项目当中注册 ActorBean
     */
    @Nullable
    Map<Integer, ActorTuple> actors;


    /**
     * 在线服务管理
     */
    @Autowired
    PlayerOnlineService onlineService;



    /**
     * 加载Spring对象
     * @param applicationContext the ApplicationContext object to be used by this object
     * @throws BeansException Bean错误
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // 检索出注册的Bean业务
        try {
            actors = ActorSearcher.searchMapping(applicationContext, GameApplication.class, this);
        } catch (IllegalAccessException e) {
            if (log.isErrorEnabled()) {
                log.error("[Game] Failed by Search Actor = {}", e.getMessage());
            }
        }

        // 打印检索的 Actor Bean 对象
        if (actors != null && log.isInfoEnabled()) {
            for (Map.Entry<Integer, ActorTuple> actor : actors.entrySet()) {
                log.info("[Game] Search Actor({}) = {}::{}",
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
        if (log.isInfoEnabled()) {
            log.info("[Game] Established = {}", session);
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
        if (log.isInfoEnabled()) {
            log.info("[Game] Close({}) = {}", status, session);
        }
        onlineService.leave(session);
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
        if (log.isErrorEnabled()) {
            log.error("[Game] Error = {} | {}", exception.getMessage(), session);
        }
        onlineService.leave(session);
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
            if(log.isErrorEnabled()){
                log.error(e.getMessage());
            }
        }
    }


    /**
     * 关闭连接
     * @param session 客户端会话
     * @param status 关闭状态
     */
    public void close(WebSocketSession session,CloseStatus status){
        if(!session.isOpen()){
            return ;
        }

        try{
            session.close(status);
        }catch (IOException exception){
            if(log.isErrorEnabled()){
                log.error(exception.getMessage());
            }
        }
    }


    /**
     * 确认是否在线
     * @param session 玩家会话
     * @return boolean
     */
    public boolean isOnline(WebSocketSession session){
        return onlineService.containsBySession(session);
    }


    /**
     * 确认是否在线
     * @param uuid 玩家会话
     * @return boolean
     */
    public boolean isOnline(String uuid){
        return onlineService.containsById(uuid);
    }


    /**
     * 获取在线会话
     * @param uuid 玩家唯一标识
     * @return WebSocketSession|Empty
     */
    public Optional<WebSocketSession> getOnline(String uuid){
        return onlineService.getBySession(uuid);
    }


    /**
     * 加入在线
     * @param session 玩家会话
     * @param uuid 玩家唯一标识
     */
    public void joinOnline(WebSocketSession session,String uuid){
        onlineService.join(session,uuid);
    }


    /**
     * 离开在线
     * @param session 玩家会话
     */
    public void leaveOnline(WebSocketSession session){
        if(session.isOpen()){
            close(session,CloseStatus.NORMAL);
        }
        onlineService.leave(session);
    }

    /**
     * 离开在线
     * @param session 玩家会话
     * @param status 关闭状态
     */
    public void leaveOnline(WebSocketSession session,CloseStatus status){
        if(session.isOpen()){
            close(session,status);
        }
        onlineService.leave(session);
    }



}

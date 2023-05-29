package com.meteorcat.mix.actors.logic;

import com.fasterxml.jackson.databind.JsonNode;
import com.meteorcat.mix.actors.ActorWebSocketApplication;
import com.meteorcat.mix.core.actor.ActorController;
import com.meteorcat.mix.core.actor.ActorMapping;
import com.meteorcat.mix.core.actor.ActorRuntime;
import com.meteorcat.mix.core.utils.JsonNodeUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * 房间Actor
 *
 * @author MeteorCat
 */
@Component
@ActorController(ActorWebSocketApplication.class)
public class RoomActor {

    /**
     * 会话列表
     */
    List<WebSocketSession> sessionList = new ArrayList<>();



    /**
     * 网关对象
     */
    @ActorRuntime
    ActorWebSocketApplication gateway;


    /**
     * 加入房间监听消息
     * @param session 会话对象
     * @param value 响应值
     * @param data 响应数据
     */
    @ActorMapping(200)
    public void join(WebSocketSession session, Integer value, JsonNode data) {
        // 如果会话列表已经存在就直接跳过
        if(sessionList.contains(session)){
            return ;
        }

        // 加入会话响应完成
        sessionList.add(session);
        gateway.response(session,value,new HashMap<>(1){{
            put("message","加入成功");
        }});
    }


    /**
     * 离开房间销毁
     * @param session 会话对象
     * @param value 响应值
     * @param data 响应数据
     */
    @ActorMapping(201)
    public void leave(WebSocketSession session, Integer value, JsonNode data){
        // 如果会话列表不存在就直接跳过
        if(!sessionList.contains(session)){
            return ;
        }


        // 退出房间和响应数据
        sessionList.remove(session);
        gateway.response(session,value,new HashMap<>(1){{
            put("message","退出成功");
        }});
    }


    /**
     * 推送消息
     * @param session 会话对象
     * @param value 响应值
     * @param data 响应数据
     */
    @ActorMapping(202)
    public void push(WebSocketSession session, Integer value, JsonNode data){
        // 如果会话列表不存在就直接跳过
        if(!sessionList.contains(session)){
            return ;
        }

        // 确定有消息字段
        Optional<String> messageOptional = JsonNodeUtil.isText(data,"message");
        if(messageOptional.isEmpty()){
            return ;
        }


        // 向其他玩家发送消息
        String message = messageOptional.get();
        sessionList
                .stream()
                .filter(WebSocketSession::isOpen)
                .filter(hit->hit!=session)
                .forEach(hit-> gateway.response(hit,value,new HashMap<>(1){{
                    put("message",message);
                }}));

        // 响应返回成功
        gateway.response(session,value,new HashMap<>(1){{
            put("message","发送成功");
        }});

    }

}

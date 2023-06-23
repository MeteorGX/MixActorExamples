package com.meteorcat.mix.gateway.service;

import com.meteorcat.mix.gateway.entity.PlayerInfoEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 玩家在线服务
 * @author MeteorCat
 */
@Service
public class PlayerOnlineService {

    /**
     * 在线玩家
     */
    Map<WebSocketSession, String> online = new HashMap<>();


    /**
     * 加入会话
     * @param session 玩家会话
     * @param uuid 玩家唯一标识
     */
    public void join(WebSocketSession session,String uuid){
        online.put(session,uuid);
    }

    /**
     * 删除挂载对象
     * @param session 玩家会话
     */
    public void leave(WebSocketSession session){
        online.remove(session);
    }


    /**
     * 确认是否在线
     * @param session 玩家会话
     * @return boolean
     */
    public boolean containsBySession(WebSocketSession session){
        return online.containsKey(session);
    }

    /**
     * 确认是否在线
     * @param uuid 玩家标识ID
     * @return boolean
     */
    public boolean containsById(String uuid){
        return online.containsValue(uuid);
    }


    /**
     * 获取在线玩家会话
     * @param uuid 玩家唯一标识
     * @return WebSocketSession|Empty
     */
    public Optional<WebSocketSession> getBySession(String uuid){
        return online
                .entrySet()
                .stream()
                .filter(hit->hit.getValue().equals(uuid))
                .map(Map.Entry::getKey)
                .findFirst();
    }

}

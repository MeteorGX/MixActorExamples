package com.meteorcat.mix.actors.logic;

import com.fasterxml.jackson.databind.JsonNode;
import com.meteorcat.mix.actors.ActorWebSocketApplication;
import com.meteorcat.mix.core.actor.ActorController;
import com.meteorcat.mix.core.actor.ActorMapping;
import com.meteorcat.mix.core.actor.ActorRuntime;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;

/**
 * 服务端Actor对象
 *
 * @author MeteorCat
 */
@Component
@ActorController(ActorWebSocketApplication.class)
public class ServerActor {


    /**
     * 网关对象
     */
    @ActorRuntime
    ActorWebSocketApplication gateway;


    /**
     * 获取服务器时间
     *
     * @param session 会话对象
     */
    @ActorMapping(100)
    public void getTimestamp(WebSocketSession session, Integer value, JsonNode data) {
        gateway.response(session, 100, new HashMap<>(1) {{
            put("timestamp", System.currentTimeMillis());
        }});
    }


}

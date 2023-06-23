package com.meteorcat.mix.gateway.game;

import com.fasterxml.jackson.databind.JsonNode;
import com.meteorcat.mix.core.actor.ActorController;
import com.meteorcat.mix.core.actor.ActorMapping;
import com.meteorcat.mix.core.actor.ActorRuntime;
import com.meteorcat.mix.core.utils.JsonNodeExtends;
import com.meteorcat.mix.gateway.GameApplication;
import com.meteorcat.mix.gateway.entity.PlayerInfoEntity;
import com.meteorcat.mix.gateway.entity.PlayerRecordEntity;
import com.meteorcat.mix.gateway.service.PlayerInfoService;
import com.meteorcat.mix.gateway.service.PlayerRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Optional;

/**
 * 登录验证服务Actor
 * @author MeteorCat
 */
@Slf4j
@Component
@ActorController(GameApplication.class)
public class AuthActor {


    /**
     * 挂载的服务类
     */
    @ActorRuntime
    GameApplication application;


    /**
     * API记录服务
     */
    @Autowired
    PlayerRecordService recordService;


    /**
     * 玩家信息服务
     */
    @Autowired
    PlayerInfoService infoService;



    /**
     * 登录验证接口
     * @param session 客户端服务
     * @param type 消息类型
     * @param data 消息数据
     */
    @ActorMapping(200)
    public void login(WebSocketSession session, Integer type, JsonNode data){
        // 确定当前是否登录, 已经登录不需要重复请求
        if(application.isOnline(session)){
            return ;
        }



        // 验证登录在线,这里需要传递 server_id + union_id + token 参数验证
        Optional<Integer> serverIdOptional = JsonNodeExtends.isInteger(data,"server_id");
        Optional<String> unionIdOptional = JsonNodeExtends.isText(data,"union_id");
        Optional<String> tokenOptional = JsonNodeExtends.isText(data,"token");
        if(serverIdOptional.isEmpty() || unionIdOptional.isEmpty() || tokenOptional.isEmpty()){
            application.close(session, CloseStatus.BAD_DATA);
            return ;
        }

        // 获取所有参数
        Integer serverId = serverIdOptional.get();
        String unionId = unionIdOptional.get();
        String token = tokenOptional.get();

        // 检索服务当中挂载的对象
        Optional<PlayerRecordEntity> entityOptional = recordService.findRecord(serverId,unionId);
        if (entityOptional.isEmpty()){
            application.close(session, CloseStatus.BAD_DATA);
            return ;
        }

        // Token 错误的时候返回异常错误码
        if(!entityOptional.get().getToken().equals(token)){
            application.close(session, CloseStatus.NOT_ACCEPTABLE);
            return ;
        }

        // 查找是否存在玩家
        Optional<PlayerInfoEntity> playerInfoEntityOptional = infoService.isPlayer(serverId,unionId);
        PlayerInfoEntity playerInfo = playerInfoEntityOptional.orElseGet(() -> infoService.createPlayer(serverId, unionId).orElse(null));
        if(playerInfo == null){
            application.close(session, CloseStatus.SERVER_ERROR);
            return ;
        }


        // 完成验证之后就是确认目前是否登录挂载在内存当中, 如果还在直接让其下线
        String uid = playerInfo.getUuid();
        Optional<WebSocketSession> onlineOpt = application.getOnline(uid);
        onlineOpt.ifPresent(webSocketSession -> application.leaveOnline(webSocketSession, CloseStatus.SERVICE_OVERLOAD));


        // 正式写入在线并返回给客户端
        application.joinOnline(session,uid);
        application.send(session,type,new HashMap<>(3){{
            put("server_id",serverId);
            put("union_id",unionId);
            put("uuid",uid);
        }});
    }

}

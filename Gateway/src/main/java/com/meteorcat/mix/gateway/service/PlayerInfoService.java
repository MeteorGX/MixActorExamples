package com.meteorcat.mix.gateway.service;

import com.meteorcat.mix.core.utils.DatetimeExtends;
import com.meteorcat.mix.gateway.entity.PlayerInfoEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * 玩家API服务
 * @author MeteorCat
 */
@Service
public class PlayerInfoService {

    /**
     * 在线玩家
     */
    Map<String,PlayerInfoEntity> players = new HashMap<>();


    /**
     * 判断是否存在在线玩家
     * @param serverId 服务器ID
     * @param unionId 玩家标识Id
     * @return PlayerInfoEntity|Empty
     */
    public Optional<PlayerInfoEntity> isPlayer(Integer serverId,String unionId){
        return players
                .values()
                .stream()
                .filter(active -> active.getServerId().equals(serverId) && active.getUnionId().equals(unionId))
                .findFirst();
    }


    /**
     * 创建玩家
     * @param serverId 服务器ID
     * @param unionId 玩家标识ID
     * @return PlayerInfoEntity|Empty
     */
    public Optional<PlayerInfoEntity> createPlayer(Integer serverId,String unionId){
        PlayerInfoEntity entity = new PlayerInfoEntity();
        String uuid = UUID.randomUUID().toString();
        entity.setUuid(uuid);
        entity.setServerId(serverId);
        entity.setUnionId(unionId);
        entity.setCreateTimestamp(System.currentTimeMillis());
        players.put(uuid,entity);
        return Optional.of(entity);
    }



}

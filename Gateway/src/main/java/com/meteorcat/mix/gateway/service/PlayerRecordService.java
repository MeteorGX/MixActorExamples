package com.meteorcat.mix.gateway.service;

import com.meteorcat.mix.core.utils.DatetimeExtends;
import com.meteorcat.mix.core.utils.DigestExtends;
import com.meteorcat.mix.gateway.entity.PlayerRecordEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 玩家登录记录服务
 * @author MeteorCat
 */
@Service
public class PlayerRecordService {

    /**
     * 登录记录
     */
    List<PlayerRecordEntity> records = new ArrayList<>();


    /**
     * 查找记录
     * @param serverId 服务器ID
     * @param unionId 玩家ID
     * @return PlayerRecordEntity|Empty
     */
    public Optional<PlayerRecordEntity> findRecord(Integer serverId,String unionId){
        return records
                .stream()
                .filter(hit->hit.getServerId().equals(serverId) && hit.getUnionId().equals(unionId))
                .findFirst();
    }


    /**
     * 替换追加登录记录
     * @param serverId 服务器ID
     * @param unionId 玩家标识ID
     * @return PlayerRecordEntity|Empty
     */
    public Optional<PlayerRecordEntity> setRecord(Integer serverId,String unionId){
        if(records.stream().anyMatch(hit -> hit.getServerId().equals(serverId) && hit.getUnionId().equals(unionId))){
            records.removeIf(hit->hit.getServerId().equals(serverId) && hit.getUnionId().equals(unionId));
        }

        // 生成全新Token
        Optional<String> token = DigestExtends.md5(UUID.randomUUID().toString().getBytes());
        if(token.isEmpty()){
            return Optional.empty();
        }

        // 更新创建记录
        PlayerRecordEntity result = new PlayerRecordEntity();
        result.setUuid(UUID.randomUUID().toString());
        result.setServerId(serverId);
        result.setUnionId(unionId);
        result.setCreateTimestamp(System.currentTimeMillis());
        result.setToken(token.get());
        records.add(result);
        return Optional.of(result);
    }

}

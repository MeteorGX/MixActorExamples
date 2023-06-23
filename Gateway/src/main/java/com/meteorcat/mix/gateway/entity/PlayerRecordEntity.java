package com.meteorcat.mix.gateway.entity;

import lombok.Data;

/**
 * 玩家SDK登录生成标识日志
 * @author MeteorCat
 */
@Data
public class PlayerRecordEntity {

    /**
     * 记录唯一标识
     */
    private String uuid;

    /**
     * 服务器ID
     */
    private Integer serverId;

    /**
     * 玩家唯一标识
     */
    private String unionId;


    /**
     * 登陆标识
     */
    private String token;


    /**
     * 创建时间
     */
    private Long createTimestamp;

}

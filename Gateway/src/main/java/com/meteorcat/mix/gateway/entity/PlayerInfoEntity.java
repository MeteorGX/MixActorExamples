package com.meteorcat.mix.gateway.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 玩家实体对象
 * @author MeteorCat
 */
@Data
public class PlayerInfoEntity implements Serializable {

    /**
     * 游戏服务器唯一标识
     */
    private String uuid;

    /**
     * SDK的唯一标识
     */
    private String unionId;

    /**
     * SDK的指定服务器
     */
    private Integer serverId;


    /**
     * 游戏金币
     */
    private Integer gold = 0;



    /**
     * 账号创建时间
     */
    private Long createTimestamp;


    /**
     * 账号登录时间
     */
    private Long loginTimestamp = 0L;
}

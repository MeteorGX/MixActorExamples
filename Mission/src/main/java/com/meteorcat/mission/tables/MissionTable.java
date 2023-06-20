package com.meteorcat.mission.tables;

import lombok.Data;

import java.io.Serializable;

/**
 * 任务表结构
 * @author MeteorCat
 */
@Data
public class MissionTable implements Serializable {

    private Integer id;

    private String key;

    private String name;

    private String description;

}

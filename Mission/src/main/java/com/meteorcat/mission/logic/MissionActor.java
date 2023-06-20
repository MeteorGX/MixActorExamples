package com.meteorcat.mission.logic;

import com.fasterxml.jackson.databind.JsonNode;
import com.meteorcat.mission.MissionWebSocketApplication;
import com.meteorcat.mission.config.JsonTablesConfig;
import com.meteorcat.mission.tables.MissionTable;
import com.meteorcat.mix.core.actor.ActorController;
import com.meteorcat.mix.core.actor.ActorMapping;
import com.meteorcat.mix.core.actor.ActorRuntime;
import com.meteorcat.mix.core.utils.JsonNodeExtends;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.core.io.buffer.DefaultDataBufferFactory.DEFAULT_INITIAL_CAPACITY;

/**
 * 任务列表
 * @author MeteorCat
 */
@Component
@ActorController(MissionWebSocketApplication.class)
public class MissionActor {

    /**
     * 日志
     */
    Logger logger = LoggerFactory.getLogger(MissionActor.class);

    /**
     * 运行的挂载类
     */
    @ActorRuntime
    MissionWebSocketApplication application;


    /**
     * 系统配置的策划表
     */
    @Autowired
    Map<String, JsonNode> configs;




    /**
     * 任务完成进度,一般是采用 @Service 挂载并记录数据库当中
     * 这里当作日常任务, 每天都会更新, 所以采用以下格式:
     *  { '202306'(年月日):{ '1':(任务id):true|false(是否完成),  } }
     *  一般来说过了今天就会更新任务完成
     */
    Map<Integer,Map<Integer,Boolean>> actives = new HashMap<>();


    /**
     * 获取年月日
     * @return Int
     */
    Integer getYmd(){
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        return Integer.parseInt(df.format(System.currentTimeMillis()));
    }


    /**
     * 获取任务列表[返回数据 -> list:任务列表, active:[ [任务id,任务是否完成0|1] ]完成列表]
     * @param session 会话对象
     * @param type 类型
     * @param data 数据
     */
    @ActorMapping(100)
    public void getMissionList(WebSocketSession session, Integer type, JsonNode data){
        if(logger.isDebugEnabled()){
            logger.debug("Get Mission: {}",session);
        }



        // 确认是否存在任务
        final String missionKey = "Mission";
        if(!configs.containsKey(missionKey)){
            application.send(session, type, new HashMap<>(1) {{
                put("list", new HashMap<>(0));
                put("active",new HashMap<>(0));
            }});
            return;
        }


        // 获取当天年月日, 取出当天的完成任务表
        Integer now = getYmd();
        application.send(session,type,new HashMap<>(1){{
            put("list",configs.get(missionKey));
            put("active",actives.containsKey(now) ? actives
                    .get(now)
                    .entrySet()
                    .stream()
                    .map(hit->new Integer[]{hit.getKey(),hit.getValue() ? 1 : 0})
                    .collect(Collectors.toList()) : new HashMap<>(0));
        }});
    }


    /**
     * 获取单一人物完成精度[只需要ID,然后返回是否完成,格式采用 {active:[任务id,1|0]} ]
     * @param session 会话
     * @param type 类型
     * @param data 数据
     */
    @ActorMapping(101)
    public void getMissionDetail(WebSocketSession session,Integer type,JsonNode data){
        // 获取id
        Optional<Integer> idOpt = JsonNodeExtends.isInteger(data,"id");
        if(idOpt.isEmpty()){
            return ;
        }

        // 获取当天年月日, 获取是否任务完成
        Integer now = getYmd();
        if(!actives.containsKey(now)){
            return ;
        }

        // 检出任务是否完成
        Integer id = idOpt.get();
        Map<Integer,Boolean> active = actives.get(now);

        // 没找到任务,直接返回
        if(!active.containsKey(id)){
            application.send(session,type,new HashMap<>(1){{
                put("active",new Integer[]{id,0});
            }});
            return;
        }

        // 找到任务直接返回
        Boolean status = active.get(id);
        application.send(session,type,new HashMap<>(1){{
            put("active",new Integer[]{id,status ? 1 : 0});
        }});
    }

    /**
     * 推送任务完成[只需要ID,然后返回更新时候的任务表]
     * @param session 会话
     * @param type 类型
     * @param data 数据
     */
    @ActorMapping(102)
    public void activeMission(WebSocketSession session,Integer type,JsonNode data){
        // 获取id
        Optional<Integer> idOpt = JsonNodeExtends.isInteger(data,"id");
        if(idOpt.isEmpty()){
            return ;
        }

        // 获取任务配置表
        final String missionKey = "Mission";
        if(!configs.containsKey(missionKey)){
            return ;
        }

        // 获取任务配置
        JsonNode node = configs.get(missionKey);
        Optional<Map<String,MissionTable>> tableOpt = JsonNodeExtends.isEntity(node,MissionTable.class);
        if(tableOpt.isEmpty()){
            return ;
        }

        // 确认是否任务id存在
        Integer id = idOpt.get();
        Map<String,MissionTable> table = tableOpt.get();
        if(!table.containsKey(Integer.toString(id))){
            return ;
        }

        // 获取当天年月日, 获取是否任务存在,不存在则创建任务记录
        Integer now = getYmd();
        if(!actives.containsKey(now)){
            actives.put(now,new HashMap<>(DEFAULT_INITIAL_CAPACITY));
        }

        // 获取当前的任务是否存在,不存在直接返回未完成
        Map<Integer,Boolean> active = actives.get(now);
        if(!active.containsKey(id)){
            actives.get(now).put(id,false);
        }

        // 确认是否完成, 未完成直接修改并推送等功能
        if(!actives.get(now).get(id)){
            // todo:这里就是准备切换状态,可以搞推送奖励和审核条件等功能


            // 修改状态
            actives.get(now).replace(id,true);
        }



        // 直接返回完成状态
        application.send(session,type,new HashMap<>(1){{
            put("active",new Integer[]{id,actives.get(now).get(id) ? 1 : 0});
        }});
    }


}

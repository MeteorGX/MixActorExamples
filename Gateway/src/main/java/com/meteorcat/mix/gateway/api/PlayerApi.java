package com.meteorcat.mix.gateway.api;

import com.meteorcat.mix.gateway.entity.PlayerRecordEntity;
import com.meteorcat.mix.gateway.service.PlayerRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Optional;


/**
 * 玩家RestAPI接口, 用于 SDK 验证登录传递过来进行创建服务端账号
 * 1. 一般都是SDK服务器自己生成 (服务器+玩家唯一码) 组合
 * 2. 然后SDK授权完全之后通过密钥混合提交到游戏网关
 * 3. 网关验证Sign之后确认游戏数据库是否存在账户, 如果不存在直接创建
 * 示例: http://localhost:8083/api/player/login?server_id=1&union_id=10001&sign=bdfdb05d9add3516190f08abeaeb15c2
 * @author MeteorCat
 */
@RestController
@RequestMapping("/api/player")
public class PlayerApi extends BaseApi {

    /**
     * 玩家登录记录服务
     */
    @Autowired
    PlayerRecordService playerRecordService;


    /**
     * SDK对玩家登录哈希的标识KEY
     */
    @Value("${mix.gateway.api.key:mix_login_key}")
    private String key;

    /**
     * 账号授权登录, 不存在则直接登录
     * @return JSON
     */
    @RequestMapping("/login")
    public Object login(
            @RequestParam(name = "server_id",defaultValue = "1") Integer serverId,
            @RequestParam(name = "union_id",defaultValue = "") String unionId,
            @RequestParam(name = "sign",defaultValue = "") String sign
    ){
        // 确定参数
        if(unionId.isBlank() || sign.isBlank()){
            return failed("参数错误[PARAM]");
        }

        // 获取哈希Sign
        String hash = createSign(key,new HashMap<>(2){{
            put("server_id",serverId);
            put("union_id",unionId);
        }});

        // 比较是否哈希匹配
        if(!hash.equals(sign)){
            return failed("参数错误[SIGN]");
        }


        // 生成登录Token提供登录完成
        Optional<PlayerRecordEntity> recordOpt = playerRecordService.setRecord(serverId,unionId);
        if(recordOpt.isEmpty()){
            return failed("服务器错误[Record]");
        }


        // 完成之后返回对应登录数据, 用于连接游戏客户端服务器
        PlayerRecordEntity record = recordOpt.get();
        return success(new HashMap<>(3){{
            put("server_id",record.getServerId());
            put("union_id",record.getUnionId());
            put("token",record.getToken());
        }});
    }


    /**
     * 手动登录,刷新登录Token
     * @return JSON
     */
    @RequestMapping("/logout")
    public Object logout(){
        return success();
    }

}

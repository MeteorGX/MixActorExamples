package com.meteorcat.mix.actors.config;

import com.meteorcat.mix.actors.ActorWebSocketApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Websocket配置
 * @author MeteorCat
 */
@Configuration
@EnableWebSocket
public class WebsocketConfig implements WebSocketConfigurer {

    /**
     * 请求路径
     */
    @Value("${mix.actors.path:/actors}")
    private String path;


    /**
     * 请求的源站路径
     */
    @Value("${mix.actors.origin:*}")
    private String origin;



    /**
     * 挂载的服务对象
     */
    @Autowired
    private ActorWebSocketApplication application;


    /**
     * 注册服务请求端点
     * @param registry 注册句柄
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(application,path).setAllowedOrigins(origin);
    }
}

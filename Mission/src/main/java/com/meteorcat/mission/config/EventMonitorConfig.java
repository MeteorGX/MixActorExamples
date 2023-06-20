package com.meteorcat.mission.config;

import com.meteorcat.mix.core.event.EventMonitor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketSession;

/**
 * 事件池配置
 * @author MeteorCat
 */
@Configuration
public class EventMonitorConfig {


    @Value("${mix.mission.event.pool.size:2}")
    private Integer eventPoolSize;


    @Bean
    EventMonitor<WebSocketSession,String> eventMonitor(){
        return new EventMonitor<>(eventPoolSize);
    }

}

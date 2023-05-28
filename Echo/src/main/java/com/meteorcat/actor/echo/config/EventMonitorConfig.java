package com.meteorcat.actor.echo.config;

import com.meteorcat.mix.core.event.EventMonitor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketSession;

/**
 * 事件管理器配置
 * @author MeteorCat
 */
@Configuration
public class EventMonitorConfig {


    /**
     * 事件池的线程数量
     */
    @Value("${mix.echo.thread:2}")
    private Integer thread;

    /**
     * 获取全局事件池
     * @return EventMonitor<WebSocketSession,String>
     */
    @Bean
    public EventMonitor<WebSocketSession,String> eventMonitor(){
        return new EventMonitor<>(thread);
    }

}

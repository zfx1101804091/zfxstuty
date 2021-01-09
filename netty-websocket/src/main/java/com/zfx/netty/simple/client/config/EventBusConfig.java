package com.zfx.netty.simple.client.config;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.zfx.netty.simple.client.events.EventSubscribe;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @description:
 * @author: zheng-fx
 * @time: 2021/1/9 0009 22:22
 */
@Configuration
public class EventBusConfig {
    
    @Bean
    public EventBus registerEventBus(){
        AsyncEventBus eventBus = new AsyncEventBus("eventBusName", Executors.newFixedThreadPool(50));
        
        eventBus.register(eventSubscribe());
        return eventBus;
    }
    
    @Bean
    public EventSubscribe eventSubscribe(){
        return new EventSubscribe();
    }
}

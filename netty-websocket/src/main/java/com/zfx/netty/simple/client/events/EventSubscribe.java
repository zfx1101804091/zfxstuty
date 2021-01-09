package com.zfx.netty.simple.client.events;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import io.netty.buffer.ByteBuf;

/**
 * @description:
 * @author: zheng-fx
 * @time: 2021/1/9 0009 22:21
 */
public class EventSubscribe {
    
    @Subscribe
    @AllowConcurrentEvents //接收并发处理
    public void processEvent(String event){
        System.out.println("监听到的事件："+event);
    }


    @Subscribe
    @AllowConcurrentEvents //接收并发处理
    public void processEvent2(ByteBuf byteBuf){
        System.out.println("监听到的事件2："+byteBuf);
    }
}

package com.zfx.netty.rpc;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: zheng-fx
 * @time: 2020/10/26 0026 23:47
 */
@Slf4j
public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);

    private ChannelHandlerContext ctx;
    private ChannelPromise promise;
    private String response;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.ctx = ctx;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof TextWebSocketFrame){
            TextWebSocketFrame frame = (TextWebSocketFrame)msg;
            response=frame.text();
            System.out.println("客户端接收的消息："+response);
            promise.setSuccess();
        }
            
    }

    public synchronized ChannelPromise sendMessage(Object message) {
        while (ctx == null) {
            try {
                TimeUnit.MILLISECONDS.sleep(1);
                //logger.error("等待ChannelHandlerContext实例化");
            } catch (InterruptedException e) {
                logger.error("等待ChannelHandlerContext实例化过程中出错",e);
            }
        }
        promise = ctx.newPromise();
        ctx.writeAndFlush(message);
        return promise;
    }

    public String getResponse() {
        return response;
    }
}

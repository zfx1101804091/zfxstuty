package com.zfx.netty.simple.server;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import sun.nio.cs.ext.MS874;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @description: Netty服务端消息处理handler
 * @author: zheng-fx
 * @time: 2020/10/24 0024 12:46
 */
@Slf4j
public class SimpleServerHandler extends SimpleChannelInboundHandler<Object> {

    //客户端组
    public static ChannelGroup channelGroup;

    static {
        channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    }

    //存ip 和 channel容器
    private static ConcurrentMap<String, Channel> channelMap = new ConcurrentHashMap<>();

    /**
     * handler 活跃状态，表示连接成功
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("与客户端 [{}] 连接成功...", ctx.channel().remoteAddress());
        TextWebSocketFrame frame = new TextWebSocketFrame("mamamamama");
        ctx.writeAndFlush(frame);
        channelGroup.add(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {

        //1、文本消息
        if (msg instanceof TextWebSocketFrame) {
            TextWebSocketFrame textFrame = (TextWebSocketFrame) msg;
            //第一次连接成功，给客户端发送消息
            sendMessageAll();
            //获取当前channel绑定的IP地址
            InetSocketAddress ipSocket = (InetSocketAddress) ctx.channel().remoteAddress();
            String address = ipSocket.getAddress().getHostAddress();
            log.info("当前连接地址：[{}] 接收的客户端消息：{}", address,textFrame.text());

            //将IP和channel的关系保存
            if (!channelMap.containsKey(address)) {
                channelMap.put(address, ctx.channel());
            }
        }

        //2、二进制消息
        if (msg instanceof BinaryWebSocketFrame) {
            log.info("收到二进制消息：" + ((BinaryWebSocketFrame) msg).content().readableBytes());
            
            BinaryWebSocketFrame binaryWebSocketFrame = new BinaryWebSocketFrame(Unpooled.buffer().writeBytes("客户端 这是服务端我给你的发送的消息".getBytes()));
            //给客户端发送的消息
            ctx.channel().writeAndFlush(binaryWebSocketFrame);
        }
        
        //ping消息
        if (msg instanceof PongWebSocketFrame) {
            log.info("客户端ping成功");
        }
        
        //关闭消息
        if (msg instanceof CloseWebSocketFrame) {
            log.info("客户端关闭，通道关闭");
            Channel channel = ctx.channel();
            channel.close();
        }
        
    }

    /**
     * 未注册状态，等待连接
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {

        log.info("正在与客户端 [{}] 连接中...", ctx.channel().remoteAddress());
    }

    /**
     * 非活跃状态，没有连接远程主机的时候。
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("与客户端 [{}] 断开连接", ctx.channel().remoteAddress());
        channelGroup.remove(ctx.channel());
    }

    /**
     * 异常处理
     *
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("连接异常：" + cause.getMessage());
        ctx.close();
    }

    /**
     * 给指定用户发内容
     * 后续可以掉这个方法推送消息给客户端
     */
    public void sendMessage(String address) {
        Channel channel = channelMap.get(address);
        String message = "你好，这是指定消息发送";
        channel.writeAndFlush(new TextWebSocketFrame(message));
    }

    /**
     * 群发消息
     */
    public void sendMessageAll() {
        String meesage = "这是群发信息";
        channelGroup.writeAndFlush(new TextWebSocketFrame(meesage));
    }
}

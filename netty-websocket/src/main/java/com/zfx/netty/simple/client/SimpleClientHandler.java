package com.zfx.netty.simple.client;

import com.google.common.eventbus.EventBus;
import com.zfx.netty.simple.client.config.SpringUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @description:
 * @author: zheng-fx
 * @time: 2020/10/24 0024 13:24
 */
@Slf4j
public class SimpleClientHandler extends SimpleChannelInboundHandler<Object> {

    //握手状态信息
    private WebSocketClientHandshaker handshaker;

    //netty自带的异步处理
    private ChannelPromise handshakeFuture;


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {

        log.info("当前握手状态" + this.handshaker.isHandshakeComplete());

        Channel channel = ctx.channel();
        FullHttpResponse response;

        //进行握手操作
        if (!this.handshaker.isHandshakeComplete()) {
            
            try {
                response = (FullHttpResponse) msg;
                //握手协议返回，设置结束握手
                this.handshaker.finishHandshake(channel,response);
                //设置成功
                this.handshakeFuture.setSuccess();
                log.info("服务端消息： {}",response.headers()); 
            }catch (WebSocketHandshakeException exception){
                response = (FullHttpResponse) msg;
                String errorMsg = String.format("握手失败,status:%s,reason:%s", response.status(), response.content().toString(CharsetUtil.UTF_8));
                this.handshakeFuture.setFailure(new Exception(errorMsg));
            }
        }else if(msg instanceof FullHttpResponse){
            response = (FullHttpResponse) msg;
            throw new IllegalStateException("Unexpected FullHttpResponse (getStatus=" + response.status() + ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
        }else {
            //接收服务端消息
            WebSocketFrame frame = (WebSocketFrame) msg;
            
            //文本消息
            if(frame instanceof TextWebSocketFrame){
                TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
                log.info("客户端接收的消息是："+textFrame.text());
                
                /**###########################################################*/
//                SpringUtil.getBean(EventBus.class).post(textFrame.text());
                SpringUtil.getBean(EventBus.class).post(Unpooled.copiedBuffer(textFrame.text().getBytes()));

            }

            //二进制信息
            if (frame instanceof BinaryWebSocketFrame) {
                BinaryWebSocketFrame binFrame = (BinaryWebSocketFrame)frame;
                log.info("BinaryWebSocketFrame"+binFrame);
            }
            
            //ping信息
            if (frame instanceof PongWebSocketFrame) {
                log.info("WebSocket Client received pong");
            }
            //关闭消息
            if (frame instanceof CloseWebSocketFrame) {
                log.info("receive close frame");
                channel.close();
            }
        }

    }

    /**
     * Handler活跃状态，表示连接成功
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("与服务端连接成功");
        
    }

    /**
     * 在Netty中，无论是服务端还是客户端，在Channel注册时都会为其绑定一个ChannelPromise
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        handlerAdded(ctx);
    }

    /**
     * 非活跃状态，没有连接远程主机的时候。
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("主机关闭");
    }

    /**
     * 异常处理
     *
     * @param ctx
     * @param cause
     * @throws Exception
     */
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("连接异常：" + cause.getMessage());
        cause.printStackTrace();
        ctx.close();
    }

    public ChannelFuture handshakeFuture(){
        return this.handshakeFuture;
    }

    public void handlerAdded(ChannelHandlerContext ctx) {
        this.handshakeFuture = ctx.newPromise();
    }

    public WebSocketClientHandshaker getHandshaker() {
        return handshaker;
    }

    public void setHandshaker(WebSocketClientHandshaker handshaker) {
        this.handshaker = handshaker;
    }

    public ChannelPromise getHandshakeFuture() {
        return handshakeFuture;
    }

    public void setHandshakeFuture(ChannelPromise handshakeFuture) {
        this.handshakeFuture = handshakeFuture;
    }
}

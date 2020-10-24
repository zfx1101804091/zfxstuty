package com.zfx.netty.simple.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @description: Netty客户端
 * @author: zheng-fx
 * @time: 2020/10/23 0023 23:55
 */
@Slf4j
public class SimpleClient {

    public static void main(String[] args) {
        new SimpleClient().start();
    }


    public void start() {

        try {
            //线程组
            NioEventLoopGroup group = new NioEventLoopGroup();
            //启动类
            Bootstrap bootstarp = new Bootstrap();

            bootstarp.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast("http-codec", new HttpClientCodec());
                            pipeline.addLast("aggregator", new HttpObjectAggregator(1024 * 1024 * 10));
                            pipeline.addLast("hookedHandler", new SimpleClientHandler());
                        }
                    });
            //websocke连接的地址，/hello是因为在服务端的websockethandler设置的
            URI websocketURI = new URI("ws://localhost:10011/hello");

            HttpHeaders httpHeaders = new DefaultHttpHeaders();
            //进行握手
            WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory
                    .newHandshaker(websocketURI,
                            WebSocketVersion.V13,
                            null,
                            true,
                            httpHeaders);
            
            //客户端与服务端连接的通道，final修饰符表示只会有一个
            final Channel channel = bootstarp.connect(websocketURI.getHost(),websocketURI.getPort()).sync().channel();
            SimpleClientHandler handler = (SimpleClientHandler)channel.pipeline().get("hookedHandler");
            
            handler.setHandshaker(handshaker);
            handshaker.handshake(channel);
            
            //阻塞等待是否握手成功
            handler.handshakeFuture().sync();
            log.info("握手成功。。。");
            //给服务端发送的内容，如果客户端与服务端连接成功后，可以多次掉用这个方法发送消息
            sengMessage(channel);
            
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }
    public static void sengMessage(Channel channel){
        //发送的内容，是一个文本格式的内容
        String putMessage="你好，我是客户端";
        TextWebSocketFrame frame = new TextWebSocketFrame(putMessage);
        channel.writeAndFlush(frame).addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (channelFuture.isSuccess()) {
                    System.out.println("消息发送成功，发送的消息是："+putMessage);
                } else {
                    System.out.println("消息发送失败 " + channelFuture.cause().getMessage());
                }
            }
        });
    }
}
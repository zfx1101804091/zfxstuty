package com.zfx.netty.simple.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * @description: Netty基本客户端
 * @author: zheng-fx
 * @time: 2020/10/24 0024 12:19
 */
@Slf4j
public class SimpleServer {

    public static void main(String[] args) {
        new SimpleServer().start();
    }
    
    //服务端启动
    public void start(){
        
        //1、创建一个线程组接收客户端的连接
        NioEventLoopGroup boosGroup =new NioEventLoopGroup();
        //2、创建一个线程组处理网络操作
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        
        try {
            //3、创建服务器启动助手来配置参数
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(boosGroup,workerGroup) //4、设置2个线程组
                    .channel(NioServerSocketChannel.class) //5、使用NioServerSocketChannel作为服务器通道的实现
                    .option(ChannelOption.SO_BACKLOG,1024*1024*10) //6、设置线程队列中等待连接的个数
                    .childOption(ChannelOption.SO_KEEPALIVE,true) //7、保持活动连接状态
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() { //8、创建一个通道初始化对象
                        //9、往pipeline链中添加自定义的handler
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            //websocket协议本身是基于Http协议的，所以需要Http解码器
                            pipeline.addLast("http-codec",new HttpServerCodec());
                            //以块的方式来写的处理器
                            pipeline.addLast("http-chunked",new ChunkedWriteHandler());
                            //netty是基于分段请求的，HttpObjectAggregator的作用是将请求分段再聚合,参数是聚合字节的最大长度
                            pipeline.addLast("aggregator",new HttpObjectAggregator(1024*1024*1024));
                            //这个是websocket的handler，是netty提供的，也可以自定义，建议就用默认的
                            pipeline.addLast(new WebSocketServerProtocolHandler("/hello",null,true,65535));
                            //自定义的handler，处理服务端传来的消息
                            pipeline.addLast(new SimpleServerHandler());
                        }
                    });

            //10、绑定端口 bind()方法是异步的 sync()方法是同步阻塞的
            //解释：绑定端口 bind 不需要等待结果，sync但是只有成功了才能往下进行
//            ChannelFuture future = serverBootstrap.bind(10011).sync();
            ChannelFuture channelFuture = serverBootstrap.bind(new InetSocketAddress(10011)).sync();
            log.info("服务器 [{}] 启动成功...",channelFuture.channel().localAddress());
            
            //11、关闭通道
            channelFuture.channel().closeFuture().sync();
            
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            boosGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}

package com.zfx.netty.simple;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @description:
 * @author: zheng-fx
 * @time: 2020/10/22 0022 00:33
 */
public class NettyServer {
    
    public static void main(String[] args) {

        /**
         * 1、创建2个线程组 bossGroup workerGroup
         * 2、bossGroup 只是处理连接请求，真正的和客户端业务处理，会交给wokerGroup完成
         * 3、两个都是无限循环
         * 4、bossGroup workerGroup含有的子线程（NioEventLoop）个数 == 默认实际CPU核数×2
         */
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        
        try {
            //创建服务端启动对象，配置参数
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            serverBootstrap.group(bossGroup,workerGroup)//设置2个线程组
                    .channel(NioServerSocketChannel.class)//使用NioSocketChannel作为服务器的通道实现
                    .option(ChannelOption.SO_BACKLOG,128) //设置线程队列等待连接个数
                    .childOption(ChannelOption.SO_KEEPALIVE,true) //设置保持活动连接状态
                    .childHandler(new ChannelInitializer<SocketChannel>(){ //创建一个通道初始化对象
                        //给pipeline设置处理器
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new NettyServerHandler());
                        }
                    }); //给workerGroup 的EventLoop对应的管道设置处理器
            System.out.println("服务器准备好了。。。");
            
            ChannelFuture channelFuture = serverBootstrap.bind(10002).sync();
            channelFuture.channel().closeFuture().sync();
            
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
   
}

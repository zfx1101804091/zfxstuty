package com.zfx.netty.rpc;

import com.zfx.netty.simple.client.SimpleClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.apache.logging.log4j.message.MapMessage.MapFormat.JSON;

/**
 * @description:
 * @author: zheng-fx
 * @time: 2020/10/27 0027 00:05
 */
public class NettyClient {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        NettyClient client = new NettyClient("127.0.0.1", 10011);
        while (true){
            Thread.sleep(5000);
            String nettyResponse = client.send("客户端发送："+ LocalDateTime.now().toString());
        }
    }
    
    //创建线程池
    private static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());


    /**
     * 日志记录
     */
    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);

    /**
     * 客户端业务处理handler
     */
    private NettyClientHandler clientHandler = new NettyClientHandler();

    /**
     * 事件池
     */
    private EventLoopGroup group = new NioEventLoopGroup();

    /**
     * 启动器
     */
    private Bootstrap bootstrap = new Bootstrap();

    /**
     * 客户端通道
     */
    private Channel clientChannel;

    /**
     * 客户端连接
     * @param host
     * @param port
     * @throws InterruptedException
     */
    public NettyClient(String host, int port) throws InterruptedException {
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        channel.pipeline().addLast("idleStateHandler", new IdleStateHandler(5, 5, 12));
                        channel.pipeline().addLast("nettyMessageDecoder", new StringEncoder());
                        channel.pipeline().addLast("nettyMessageEncoder", new StringDecoder());
                        channel.pipeline().addLast("clientHandler", clientHandler);
                    }
                });

        //发起同步连接操作
        ChannelFuture channelFuture = bootstrap.connect(host, port);

        //注册连接事件
        channelFuture.addListener((ChannelFutureListener)future -> {
            //如果连接成功
            if (future.isSuccess()) {
                logger.info("客户端[" + channelFuture.channel().localAddress().toString() + "]已连接...");
                clientChannel = channelFuture.channel();
            }
            //如果连接失败，尝试重新连接
            else{
                logger.info("客户端[" + channelFuture.channel().localAddress().toString() + "]连接失败，重新连接中...");
                future.channel().close();
                bootstrap.connect(host, port);
            }
        });

        //注册关闭事件
        channelFuture.channel().closeFuture().addListener(cfl -> {
            close();
            logger.info("客户端[" + channelFuture.channel().localAddress().toString() + "]已断开...");
        });
    }

    /**
     * 客户端关闭
     */
    private void close() {
        //关闭客户端套接字
        if(clientChannel!=null){
            clientChannel.close();
        }
        //关闭客户端线程组
        if (group != null) {
            group.shutdownGracefully();
        }
    }

    /**
     * 客户端发送消息
     * @param message
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public String send(String message) throws InterruptedException, ExecutionException {
        ChannelPromise promise = clientHandler.sendMessage(message);
        promise.await(3, TimeUnit.SECONDS);
        return clientHandler.getResponse();
    }
}
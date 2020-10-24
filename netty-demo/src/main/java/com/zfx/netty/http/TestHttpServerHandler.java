package com.zfx.netty.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;

/**
 * 说明：
 *  1\SimpleChannelInboundHandler 是 ChannelInboundHandlerAdapter
 *  2\HttpObject 客户端与服务端相互通信的数据被封装为 HttpObject
 */
@Slf4j
public class TestHttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {

    /**
     * 读取客户端数据
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {

        //判断msg是不是 HttpRequest请求
        if(msg instanceof HttpRequest){
            log.info("msg 类型 = ·「{}」",msg.getClass());
            log.info("客户端地址: "+ctx.channel().remoteAddress().toString().substring(1));

            //获取
            HttpRequest httpRequest = (HttpRequest) msg;
            //获取URI
            URI uri = new URI(httpRequest.uri());
            if("/favicon.ico".equals(uri.getPath())){
                log.info("请求了 favicon.ico ，不做响应");
                return;
            }

            //回复消息给客户端··「Http协议」
            ByteBuf content = Unpooled.copiedBuffer("hello,我是服务器", CharsetUtil.UTF_8);

            //构造一个http的响应（httpresponse）
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content);

            response.headers().set(HttpHeaderNames.CONTENT_TYPE,"text/plain");
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH,content.readableBytes());

            //将构建好的response返回
            ctx.writeAndFlush(response);

        }

    }
}

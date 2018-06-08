package com.alibaba.dubbo.perform.demo.agent;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class ServerTwoHandler extends ChannelInboundHandlerAdapter {
    Logger logger = LoggerFactory.getLogger(ServerTwoHandler.class);
    AtomicInteger responseInt = new AtomicInteger(2);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        byte[] req = new byte[buf.readableBytes()];
        buf.readBytes(req);
        String body = new String(req, "utf-8");
        System.out.println("get message : " + body);
        logger.info("get message : {}", body);

        String response = String.valueOf(responseInt);
        System.out.println("send message to client : " + response);

        ctx.writeAndFlush(Unpooled.copiedBuffer(response.getBytes()));
    }
}

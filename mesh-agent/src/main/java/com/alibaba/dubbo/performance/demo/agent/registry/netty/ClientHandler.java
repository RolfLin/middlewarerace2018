package com.alibaba.dubbo.performance.demo.agent.registry.netty;

import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcFuture;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcRequestHolder;
import com.alibaba.dubbo.performance.demo.agent.registry.ClientFuture;
import com.alibaba.dubbo.performance.demo.agent.registry.ClientRequestHolder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//public class ClientHandler extends ChannelInboundHandlerAdapter {
public class ClientHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private Logger logger = LoggerFactory.getLogger(ClientHandler.class);

    @Override
    public void channelRead0(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
//        ByteBuf buf = (ByteBuf) msg;
//        byte[] req = new byte[buf.readableBytes()];
        byte[] req = new byte[in.readableBytes()];

        in.readBytes(req);
        String body = new String(req, "utf-8");
        logger.info("get client message : {}", body);

        String[] strs = body.split("/");
        logger.info("split client message : {}, requestId : {}", strs[0], strs[1]);
        String requestId = strs[1];
        ClientFuture future = ClientRequestHolder.get(requestId);
        if(null != future){
            ClientRequestHolder.remove(requestId);
            future.done(strs[0]);
        }
//        in.release();
//        ctx.close();
        System.out.println("ClientHandler said :" + body);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}

package com.alibaba.dubbo.performance.demo.agent.registry.netty;

import com.alibaba.dubbo.performance.demo.agent.dubbo.RpcClient;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.Request;
import com.alibaba.dubbo.performance.demo.agent.registry.model.RequestBody;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//@ChannelHandler.Sharable
public class ServiceHandler extends ChannelInboundHandlerAdapter {

    private Logger logger = LoggerFactory.getLogger(ServiceHandler.class);
    private RpcClient rpcClient = new RpcClient();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        byte[] req = new byte[buf.readableBytes()];
        buf.readBytes(req);
        String body = new String(req, "utf-8");
        logger.info("get client message : {}", body);

        String[] strs = body.split(",");
        logger.info("split client message : {}", strs.toString());
        if (strs.length != 5) {
            return;
        }
        RequestBody reqBody = new RequestBody(strs[0], strs[1], strs[2], strs[3]);

        byte[] result = (byte[]) rpcClient.invoke(reqBody.getInterfaceName(), reqBody.getMethod(), reqBody.getParameterTypesString()
                , reqBody.getParameter());

        String resultStr = new String(result) + "/" + strs[4];
        logger.info("return ProviderHandler result : {}", resultStr);

        ctx.writeAndFlush(Unpooled.copiedBuffer((resultStr.getBytes())));
        buf.release();
        ctx.channel().close();
//        ctx.writeAndFlush(Unpooled.copiedBuffer((resultStr.getBytes()))).addListener(ChannelFutureListener.CLOSE);
    }

//    @Override
//    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//        logger.info("ServerHandler exception !");
//        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
//    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
        cause.printStackTrace();
        logger.info("ServerHandler exception !");
        ctx.close();
    }
}

package com.alibaba.dubbo.performance.demo.agent.registry.netty;

import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcFuture;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcRequestHolder;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceHandler extends SimpleChannelInboundHandler<RpcResponse> {

    private Logger logger = LoggerFactory.getLogger(ServiceHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("Netty rocks!");
//        RequestBody requestBody = new FormBody.Builder()
//                .add("interface",interfaceName)
//                .add("method",method)
//                .add("parameterTypesString",parameterTypesString)
//                .add("parameter",parameter)
//                .build();

//        okhttp3.Request request = new okhttp3.Request.Builder()
//                .url(url)
//                .post(requestBody)
//                .build();


//        RpcFuture future = new RpcFuture();

//        RpcRequestHolder.put(String.valueOf(request.getId()),future);

//        ctx.writeAndFlush(requestBody);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {
        System.out.println(response.toString());
    }
}

package com.alibaba.dubbo.performance.demo.agent.registry.netty;

import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcFuture;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcRequestHolder;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ServiceHandler extends SimpleChannelInboundHandler<RpcResponse> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse response) throws Exception {
//        String requestId = response.getRequestId();
//        RpcFuture future = RpcRequestHolder.get(requestId);
//        if(null != future){
////            RpcRequestHolder.remove(requestId);
//            future.done(response);
//        }
    }
}

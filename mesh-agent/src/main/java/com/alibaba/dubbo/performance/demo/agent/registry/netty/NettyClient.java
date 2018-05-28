package com.alibaba.dubbo.performance.demo.agent.registry.netty;

import com.alibaba.dubbo.performance.demo.agent.dubbo.model.JsonUtils;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.Request;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcFuture;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcInvocation;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcRequestHolder;

import com.alibaba.dubbo.performance.demo.agent.registry.IRegistry;
import io.netty.channel.Channel;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class NettyClient {

    private Logger logger = LoggerFactory.getLogger(NettyClient.class);

    private ServiceConnectManager serviceConnectManager;

    public NettyClient(IRegistry registry){
        this.serviceConnectManager = new ServiceConnectManager();
    }

    public Object invoke(String interfaceName, String method, String parameterTypesString, String parameter, String host, Integer post) throws Exception {

        Channel channel = serviceConnectManager.getChannel(host, post);

//        RpcInvocation invocation = new RpcInvocation();
//        invocation.setMethodName(method);
//        invocation.setAttachment("interface", interfaceName);
//        invocation.setParameterTypes(parameterTypesString);    // Dubbo内部用"Ljava/lang/String"来表示参数类型是String
//
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));
//        JsonUtils.writeObject(parameter, writer);
//        invocation.setArguments(out.toByteArray());
//
//        Request request = new Request();
//        request.setVersion("2.0.0");
//        request.setTwoWay(true);
//        request.setData(invocation);
//
//        logger.info("requestId=" + request.getId());

        RequestBody requestBody = new FormBody.Builder()
                .add("interface",interfaceName)
                .add("method",method)
                .add("parameterTypesString",parameterTypesString)
                .add("parameter",parameter)
                .build();

        okhttp3.Request request = new okhttp3.Request.Builder()
//                .url(url)
                .post(requestBody)
                .build();


        RpcFuture future = new RpcFuture();
//        RpcRequestHolder.put(String.valueOf(request.getId()),future);

        channel.writeAndFlush(request);

        Object result = null;
        try {
            result = future.get();
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }
}

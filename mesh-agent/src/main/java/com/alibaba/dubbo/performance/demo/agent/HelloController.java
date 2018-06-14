package com.alibaba.dubbo.performance.demo.agent;

import com.alibaba.dubbo.performance.demo.agent.dubbo.RpcClient;
import com.alibaba.dubbo.performance.demo.agent.netty.EchoClient;
import com.alibaba.dubbo.performance.demo.agent.netty.EchoServer;
import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import com.alibaba.dubbo.performance.demo.agent.registry.EtcdRegistry;
import com.alibaba.dubbo.performance.demo.agent.registry.IRegistry;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Random;

@RestController
public class HelloController {

    private Logger logger = LoggerFactory.getLogger(HelloController.class);
    
    private IRegistry registry = new EtcdRegistry(System.getProperty("etcd.url"));

    private RpcClient rpcClient = new RpcClient(registry);
    private Random random = new Random();
    private List<Endpoint> endpoints = null;
    private Object lock = new Object();
    private OkHttpClient httpClient = new OkHttpClient();


    @RequestMapping(value = "")
    public Object invoke(@RequestParam("interface") String interfaceName,
                         @RequestParam("method") String method,
                         @RequestParam("parameterTypesString") String parameterTypesString,
                         @RequestParam("parameter") String parameter) throws Exception {
        String type = System.getProperty("type");   // 获取type参数
        if ("consumer".equals(type)){
            return consumer(interfaceName,method,parameterTypesString,parameter);
        }
        else if ("provider".equals(type)){
            return provider(interfaceName,method,parameterTypesString,parameter);
        }else {
            return "Environment variable type is needed to set to provider or consumer.";
        }
    }

    public byte[] provider(String interfaceName,String method,String parameterTypesString,String parameter) throws Exception {
        // default by dubbo contact
        // provider agent to provider
        Object result = rpcClient.invoke(interfaceName,method,parameterTypesString,parameter);
        return (byte[]) result;
    }

    public Integer consumer(String interfaceName,String method,String parameterTypesString,String parameter) throws Exception {
        // default by http contact, spring bootstrap

        if (null == endpoints){
            synchronized (lock){
                if (null == endpoints){
                    endpoints = registry.find("com.alibaba.dubbo.performance.demo.provider.IHelloService");
                }
            }
        }

        // take the endpoiunt by 1:2:3
//        Endpoint endpoint;
//        int endpointsNum = random.nextInt(6);
//        if(endpoint.getHost().equals("10.10.10.5")){
//            endpoint = endpoints.get(2);
//        }
//        else if(endpointsNum >= 1 && endpointsNum <= 3){
//            endpoint = endpoints.get(1);
//        }
//        else{
//            endpoint = endpoints.get(0);
//        }

        // 简单的负载均衡，随机取一个
        int pointNum = random.nextInt(endpoints.size());
        Endpoint endpoint = endpoints.get(pointNum);
        logger.info("current point : " + pointNum + ", current host : " + endpoint.getHost());
        String url =  "http://" + endpoint.getHost() + ":" + endpoint.getPort();

        //consumer agent to consumer
        RequestBody requestBody = new FormBody.Builder()
                .add("interface",interfaceName)
                .add("method",method)
                .add("parameterTypesString",parameterTypesString)
                .add("parameter",parameter)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

//        int result = new EchoClient(endpoint.getHost(), endpoint.getPort()).start();
//        return result;

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            byte[] bytes = response.body().bytes();
            String s = new String(bytes);
            return Integer.valueOf(s);
        }
    }
}

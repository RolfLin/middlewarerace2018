package com.alibaba.dubbo.performance.demo.agent;

import com.alibaba.dubbo.performance.demo.agent.dubbo.RpcClient;
import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import com.alibaba.dubbo.performance.demo.agent.registry.EtcdRegistry;
import com.alibaba.dubbo.performance.demo.agent.registry.IRegistry;
import com.alibaba.dubbo.performance.demo.agent.registry.netty.ConsumerClient;
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
//      private IRegistry registry = new EtcdRegistry("http://10.21.25.133:8080");
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
//        System.out.println("ssss");
        if ("consumer".equals(type)) {
            return consumer(interfaceName, method, parameterTypesString, parameter);
        } else {
            return "Environment variable type is needed to set to provider or consumer.";
        }
    }

    public byte[] provider(String interfaceName, String method, String parameterTypesString, String parameter) throws Exception {

        Object result = rpcClient.invoke(interfaceName, method, parameterTypesString, parameter);
        logger.info("pa return : {}, {}", (byte[])result, result);
        return (byte[]) result;
    }

    public Integer consumer(String interfaceName, String method, String parameterTypesString, String parameter) throws Exception {
        logger.info("consumer agent!");
        if (null == endpoints) {
            synchronized (lock) {
                if (null == endpoints) {
                    endpoints = registry.find("com.alibaba.dubbo.performance.demo.provider.IHelloService");
                }
            }
        }

        // 简单的负载均衡，随机取一个
//        Endpoint endpoint = getEndPoint(endpoints);
        Endpoint endpoint = endpoints.get(random.nextInt(endpoints.size()));
        logger.info("provider agent port : {}", endpoint.getPort());
        //netty
        String s = (String) new ConsumerClient(endpoint.getHost(), endpoint.getPort()).start(interfaceName, method, parameterTypesString, parameter);
        logger.info("return result : {} ", s);
        return Integer.valueOf(s);

//        Object result = nettyClient.invokgie(interfaceName,method,parameterTypesString,parameter,endpoint.getHost(), endpoint.getPort());
//        logger.info(result.toString());
//        String s = new String((byte[]) result);
//        logger.info(s);
//        return Integer.valueOf(s);
//        String url =  "http://" + endpoint.getHost() + ":" + endpoint.getPort();
//
//        RequestBody requestBody = new FormBody.Builder()
//                .add("interface",interfaceName)
//                .add("method",method)
//                .add("parameterTypesString",parameterTypesString)
//                .add("parameter",parameter)
//                .build();
//
//        Request request = new Request.Builder()
//                .url(url)
//                .post(requestBody)
//                .build();
//
//        try (Response response = httpClient.newCall(request).execute()) {
//            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
//            byte[] bytes = response.body().bytes();
//            String s = new String(bytes);
//            logger.info("responseBody" + response.body() + s + " " + bytes);
//            return Integer.valueOf(s);
//        }


    }

    //RoundRobin
//    public static Endpoint getEndPoint(List<Endpoint> endpoints) {
//        Endpoint endpoint;
//        synchronized (pos) {
//            if(pos >= endpoints.size()) {
//                pos = 0;
//            }
//            endpoint = endpoints.get(pos);
//            pos++;
//        }
//        return endpoint;

}

package com.alibaba.dubbo.perform.demo.agent;

import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcFuture;

import java.util.concurrent.ConcurrentHashMap;

public class RequestHolder {

    // key: requestId     value: RpcFuture
    private static ConcurrentHashMap<String,FutureText> processingRpc = new ConcurrentHashMap<>();

    public static void put(String requestId,FutureText future){
        processingRpc.put(requestId,future);
    }

    public static FutureText get(String requestId){
        return processingRpc.get(requestId);
    }

    public static void remove(String requestId){
        processingRpc.remove(requestId);
    }
}

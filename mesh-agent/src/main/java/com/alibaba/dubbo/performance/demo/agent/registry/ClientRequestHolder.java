package com.alibaba.dubbo.performance.demo.agent.registry;

import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcFuture;

import java.util.concurrent.ConcurrentHashMap;

public class ClientRequestHolder {

    // key: requestId     value: RpcFuture
    private static ConcurrentHashMap<String,ClientFuture> processingRpc = new ConcurrentHashMap<>();

    public static void put(String requestId,ClientFuture rpcFuture){
        processingRpc.put(requestId,rpcFuture);
    }

    public static ClientFuture get(String requestId){
        return processingRpc.get(requestId);
    }

    public static void remove(String requestId){
        processingRpc.remove(requestId);
    }
}

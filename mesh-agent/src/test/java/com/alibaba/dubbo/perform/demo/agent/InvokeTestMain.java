package com.alibaba.dubbo.perform.demo.agent;

import com.alibaba.dubbo.performance.demo.agent.AgentApp;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class InvokeTestMain {

    public static void main(String[] args) {
        SpringApplication.run(InvokeTestMain.class, args);
    }
}

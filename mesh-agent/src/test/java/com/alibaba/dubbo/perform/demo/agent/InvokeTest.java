package com.alibaba.dubbo.perform.demo.agent;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InvokeTest {

    static {
        try {
            Server.start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("text");
    }



    @RequestMapping(value = "/")
    public void invoke(@RequestParam(value = "test") String test) throws InterruptedException {
        Server.start();
    }

}

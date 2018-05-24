package com.alibaba.dubbo.perform.demo.agent;

import org.junit.Test;

public class HelloController {

    private static Integer pos = 0;

    @Test
    public void testWeightRound() {
        pos++;
        System.out.println(pos);
    }
}

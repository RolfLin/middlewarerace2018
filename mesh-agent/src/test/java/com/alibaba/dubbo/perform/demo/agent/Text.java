package com.alibaba.dubbo.perform.demo.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Text {

    static Logger logger = LoggerFactory.getLogger(Text.class);
    public static void main(String[] args) {
        byte[] s = {52, 54, 51, 56, 54, 54, 53, 52, 56};
//        byte[] bytes = (byte[]) s;
//        byte[] b = bytes;
        String str = new String(s);
//        logger.info("{}", str.getBytes());
        int i = Integer.valueOf(str);
        System.out.println(s);
        System.out.println(i);
        logger.info("{}", s);
        int testLength = 1<<11;
        System.out.println(testLength);
    }
}

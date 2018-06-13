package com.alibaba.dubbo.perform.demo.agent;

public class Text {
    public static void main(String[] args) {
        Object s = "[52, 54, 51, 56, 54, 54, 53, 52, 56]";
        byte[] bytes = (byte[]) s;
        byte[] b = bytes;
        String str = new String(b);
        int i = Integer.valueOf(str);
        System.out.println(bytes);
        System.out.println(i);
    }
}

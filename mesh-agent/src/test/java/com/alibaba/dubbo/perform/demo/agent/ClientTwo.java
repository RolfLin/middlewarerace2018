package com.alibaba.dubbo.perform.demo.agent;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ClientTwo {
    private int port;

    public ClientTwo(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws InterruptedException {
//        new ClientTwo(8688).start();
    }

    public void start() throws InterruptedException {
//        System.out.println(str);
        EventLoopGroup workGroup = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(workGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, false)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel sc) throws Exception {
                        sc.pipeline().addLast(new ClientHandler());
                    }
                });
        ChannelFuture chf = b.connect("127.0.0.1", port).sync();
        String msg = "I am Client2!!!";

//        System.out.println(future.get());
//        Runnable runnable = new Runnable() {
//            @Override
//            public void run() {
//                System.out.println("run other Thread!!");
//                try {
//                    if (future.get() != null) {
//                        chf.addListener(ChannelFutureListener.CLOSE);
//                    }
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        };
//        new Thread(runnable).start();
        chf.channel().writeAndFlush(Unpooled.copiedBuffer(msg.getBytes()));
        chf.channel().closeFuture().sync();

        workGroup.shutdownGracefully();


    }
}

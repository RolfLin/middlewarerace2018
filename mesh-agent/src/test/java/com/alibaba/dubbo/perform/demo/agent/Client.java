package com.alibaba.dubbo.perform.demo.agent;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class Client {

    private int port;

    public Client(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws InterruptedException {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    new Client(8688).start();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        for (int i = 0; i < 1; i++) {
            new Thread(runnable).start();
        }
    }

    public void start() throws InterruptedException {
        EventLoopGroup workGroup = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(workGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel sc) throws Exception {
                        sc.pipeline().addLast(new ClientHandler());
                    }
                });
        ChannelFuture chf = b.connect("127.0.0.1", port).sync();
        String msg = "I am Client!!!";



        chf.channel().writeAndFlush(Unpooled.copiedBuffer(msg.getBytes()));
        chf.channel().closeFuture().sync();

        workGroup.shutdownGracefully();
    }
}

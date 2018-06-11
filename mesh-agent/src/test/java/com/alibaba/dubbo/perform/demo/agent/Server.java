package com.alibaba.dubbo.perform.demo.agent;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.web.bind.annotation.RestController;


public class Server {



    public static void main(String[] args) throws InterruptedException {
        start();
    }

    public static void start() throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel sc) throws Exception {
                        sc.pipeline().addLast(new ServerHandler());
                        sc.pipeline().addLast(new ServerTwoHandler());
                    }
                });

        ChannelFuture chf = bootstrap.bind(8688).sync();
//        FutureText future = new FutureText();
//        RequestHolder.put("1", future);

        chf.channel().closeFuture().sync();
//        System.out.println(future.get());
        bossGroup.shutdownGracefully();
        workGroup.shutdownGracefully();
    }
}

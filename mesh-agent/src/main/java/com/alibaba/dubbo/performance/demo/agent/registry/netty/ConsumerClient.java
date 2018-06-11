package com.alibaba.dubbo.performance.demo.agent.registry.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;

public class ConsumerClient  {
    private final String host;
    private final int port;

    public ConsumerClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public Object start(String interfaceName, String method, String parameterTypesString, String parameter) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        Object result = null;
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ClientHandler());
                        }
                    });
            ChannelFuture chf = b.connect(host, port).sync();
            String msg = interfaceName + "," + method + "," + parameterTypesString + "," + parameter;
            chf.channel().writeAndFlush(Unpooled.copiedBuffer(msg.getBytes()));
            result = chf.get();
            chf.channel().closeFuture().sync();


        } catch (ExecutionException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully().sync();
        }
        return result;
    }
}

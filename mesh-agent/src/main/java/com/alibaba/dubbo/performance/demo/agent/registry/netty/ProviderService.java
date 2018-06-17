package com.alibaba.dubbo.performance.demo.agent.registry.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

public class ProviderService {

//    private static final int port = 30000;
      private static int port = Integer.valueOf(System.getProperty("server.port"));
      private static String DELIMITER = "|";
//    private static EventLoopGroup bossGroup = new NioEventLoopGroup();
//    private static EventLoopGroup workGroup = new NioEventLoopGroup();

    public static void start() throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup(4);
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.SO_REUSEADDR,true)
//                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .localAddress(new InetSocketAddress(port))
//                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,3000)
//                .childOption(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel sc) throws Exception {
//                        sc.pipeline().addLast("idleStateHandler",new IdleStateHandler(500,500,500, TimeUnit.MILLISECONDS));
                        sc.pipeline().addLast(new DelimiterBasedFrameDecoder(1024, Unpooled.copiedBuffer(DELIMITER.getBytes())));
//                        sc.pipeline().addLast(new LineBasedFrameDecoder(1024));
                        sc.pipeline().addLast(new ServiceHandler());
                    }
                });

        ChannelFuture chf = bootstrap.bind().sync();

        chf.channel().closeFuture().sync();

        bossGroup.shutdownGracefully().sync();
        workGroup.shutdownGracefully().sync();
    }
}

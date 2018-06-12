package com.alibaba.dubbo.performance.demo.agent.registry.netty;

import com.alibaba.dubbo.performance.demo.agent.registry.ClientFuture;
import com.alibaba.dubbo.performance.demo.agent.registry.ClientRequestHolder;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

public class ConsumerClient  {
    private static AtomicLong atomicLong = new AtomicLong();
    Logger logger = LoggerFactory.getLogger(ConsumerClient.class);
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
            long requestId = atomicLong.getAndIncrement();
            String msg = interfaceName + "," + method + "," + parameterTypesString + "," + parameter + "," + requestId;
            logger.info("send message : {} , requestId : {}", msg, requestId);
            ClientFuture future = new ClientFuture();
            ClientRequestHolder.put(String.valueOf(requestId),future);
            chf.channel().writeAndFlush(Unpooled.copiedBuffer(msg.getBytes()));

//            chf.channel().closeFuture().sync();
            result = future.get();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully().sync();
        }
        logger.info("consumer result : {}", result);
//        return result;
       return result;
    }
}

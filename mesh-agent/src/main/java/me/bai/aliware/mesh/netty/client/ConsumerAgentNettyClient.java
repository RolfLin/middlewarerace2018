package me.bai.aliware.mesh.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import me.bai.aliware.mesh.ChannelTypeFactory;
import me.bai.aliware.mesh.ConsumerPoints;
import me.bai.aliware.mesh.netty.beans.NettyResponse;
import me.bai.aliware.mesh.netty.beans.decoder.NettyResponseDecoder;
import me.bai.aliware.mesh.registry.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static me.bai.aliware.mesh.Constant.CPU;

public class ConsumerAgentNettyClient {
	private final static Logger LOGGER = LoggerFactory.getLogger(ConsumerAgentNettyClient.class);
	private EventLoopGroup eventLoopGroup;

	public ConsumerAgentNettyClient(EventLoopGroup eventLoopGroup) {
		this.eventLoopGroup = eventLoopGroup;
	}

	private Bootstrap bootstrap = new Bootstrap()
//			.option(ChannelOption.TCP_NODELAY, true)
//			.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
			.channel(ChannelTypeFactory.getSocketChannelClass())
			.handler(new ChannelInitializer() {
				@Override
				protected void initChannel(Channel ch) throws Exception {
					//in
					ch.pipeline().addLast(new NettyResponseDecoder());
					ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
						@Override
						public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
							NettyResponse nettyResponse = (NettyResponse) msg;
							ConsumerPoints.getHttpServer().response(nettyResponse.getHttpReqId(), msg);
						}
					});

					//out
					ch.pipeline().addLast(new NettySendRequestHandler());
				}
			});


	public void connectWithEventLoopGroup() {
		LOGGER.info("开始创建netty客户端并连接provider agent...");
		List<Endpoint> endpoints = ConsumerPoints.getEndpointUtil().find();
		for (Endpoint endpoint : endpoints) {
			for (int i = 0; i < CPU; i++) {
				try {
					EventLoop eventLoop = eventLoopGroup.next();
					ChannelFuture f = bootstrap.clone(eventLoop).connect(endpoint.getHost(), endpoint.getPort()).sync();
					f.addListener((ChannelFutureListener) future -> {
						Channel channel = future.channel();
						endpoint.putChannel(eventLoop, channel);
						LOGGER.info("netty客户端初始化连接:endpoint={},eventLoop={},channel={},channel.eventLoop={}", endpoint, eventLoop, channel, channel.eventLoop());
					});
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}


}

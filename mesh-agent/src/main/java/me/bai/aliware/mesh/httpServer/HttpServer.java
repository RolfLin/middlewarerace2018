package me.bai.aliware.mesh.httpServer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import me.bai.aliware.mesh.ChannelTypeFactory;
import me.bai.aliware.mesh.ConsumerPoints;
import me.bai.aliware.mesh.netty.beans.NettyRequest;
import me.bai.aliware.mesh.test.TimeRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

public class HttpServer {
	private static final Logger LOGGER = LoggerFactory.getLogger(HttpServer.class);
	private int port;

	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;

	private static final ConcurrentHashMap<Integer, Channel> CHANNEL_CENTER = new ConcurrentHashMap<>();

	public HttpServer(EventLoopGroup bossGroup, EventLoopGroup workerGroup) {
		this.bossGroup = bossGroup;
		this.workerGroup = workerGroup;
		String portString = System.getProperty("netty.server.port");
		if (portString != null) {
			port = Integer.valueOf(portString);
		}
	}


	public void init() {
		ConsumerPoints.getNettyClient().connectWithEventLoopGroup();
		try {
			ServerBootstrap serverBootstrap = new ServerBootstrap()
					.group(bossGroup, workerGroup)
//					.option(ChannelOption.TCP_NODELAY, true)
//					.option(ChannelOption.SO_BACKLOG, 1024)
//					.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
					.channel(ChannelTypeFactory.getServerSocketChannelClass())  //bossGroup的通道，只是负责连接
					.childHandler(new ChannelInitializer() {
						@Override
						protected void initChannel(Channel ch) throws Exception {
							if (LOGGER.isDebugEnabled()) LOGGER.debug("初始化通道[{}]", ch.id());
							ChannelPipeline pipeline = ch.pipeline();
							//in
							pipeline.addLast(new HttpRequestDecoder());
							pipeline.addLast(new HttpObjectAggregator(1200));
							pipeline.addLast(new HttpObjectToNettyRequestHandler());
							pipeline.addLast(new ChannelInboundHandlerAdapter() {
								@Override
								public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
									NettyRequest nettyRequest = (NettyRequest) msg;
									int httpReqId = nettyRequest.getHttpReqId();
									CHANNEL_CENTER.put(httpReqId, ctx.channel());
									ConsumerPoints
											.getEndpointUtil()
											.pickOneEndpoint(httpReqId)
											.getChannel(ch.eventLoop())
											.writeAndFlush(nettyRequest);
									TimeRecorder.mark(nettyRequest.getHttpReqId(), "ntyReqWrt");
								}

//								@Override
//								public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//									ConsumerPoints.getEndpointUtil().pickOneEndpoint().getChannel(ch.eventLoop()).flush();
//								}

							});

							//out
							pipeline.addLast(new HttpResponseEncoder());
							pipeline.addLast(new NettyResponseToHttpResponseHandler());
						}
					});

			ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
			channelFuture.addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					LOGGER.info("在端口[{}]创建http server 成功！", port);
				}
			});
			channelFuture.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}


	public void response(int httpReqId, Object msg) {
		Channel ch = CHANNEL_CENTER.remove(httpReqId);
		ch.writeAndFlush(msg);
		if (LOGGER.isDebugEnabled()) LOGGER.debug("response httpReqId:{}", httpReqId);
	}


}

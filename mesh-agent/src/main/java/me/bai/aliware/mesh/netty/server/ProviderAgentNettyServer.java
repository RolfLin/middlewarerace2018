package me.bai.aliware.mesh.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import me.bai.aliware.mesh.ChannelTypeFactory;
import me.bai.aliware.mesh.netty.beans.decoder.NettyRequestDecoder;
import me.bai.aliware.mesh.netty.beans.encoder.NettyResponseEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static me.bai.aliware.mesh.Constant.CPU;

public class ProviderAgentNettyServer extends Thread {
	private static final Logger LOGGER = LoggerFactory.getLogger(ProviderAgentNettyServer.class);
	private int port = 30000;

	public ProviderAgentNettyServer() {
		String portString = System.getProperty("netty.server.port");
		if (portString != null) {
			this.port = Integer.valueOf(portString);
			LOGGER.info("采用传参进来的netty.server.port：{}", portString);
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				loop();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(2000);
			} catch (InterruptedException ignore) {
			}
		}
	}

	private void loop() {
		EventLoopGroup bossGroup = ChannelTypeFactory.newEventLoopGroup();
		EventLoopGroup workerGroup = ChannelTypeFactory.newEventLoopGroup(1);
//		EventLoopGroup workerGroup = ChannelTypeFactory.newEventLoopGroup(CPU);
		try {
			ServerBootstrap serverBootstrap = new ServerBootstrap();
			serverBootstrap.group(bossGroup, workerGroup)
					.option(ChannelOption.TCP_NODELAY, false)//???
//					.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
					.channel(ChannelTypeFactory.getServerSocketChannelClass())
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							LOGGER.info("netty server监听到新连接：{}", ch);

							//in
							ch.pipeline().addLast(new NettyRequestDecoder());
							ch.pipeline().addLast(new NettyRequestSendHandler());
							//out
							ch.pipeline().addLast(new NettyResponseEncoder());
						}
					});

			ChannelFuture channelFuture = serverBootstrap.bind(port).sync().addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					LOGGER.info("在端口[{}]创建netty server成功！", port);
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

}

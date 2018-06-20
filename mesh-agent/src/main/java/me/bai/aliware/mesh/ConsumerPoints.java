package me.bai.aliware.mesh;

import io.netty.channel.EventLoopGroup;
import me.bai.aliware.mesh.httpServer.HttpServer;
import me.bai.aliware.mesh.netty.client.ConsumerAgentNettyClient;
import me.bai.aliware.mesh.registry.EndpointUtil;

import static me.bai.aliware.mesh.Constant.CPU;

//import com.alibaba.dubbo.performance.demo.agent.test.MockConsumerAgentNettyClient;

public class ConsumerPoints {
	private static HttpServer httpServer;
	private static ConsumerAgentNettyClient nettyClient;
	private static EndpointUtil endpointUtil;

	public static void init() {
		EventLoopGroup bossGroup = ChannelTypeFactory.newEventLoopGroup(1);
		EventLoopGroup workerGroup = ChannelTypeFactory.newEventLoopGroup(CPU);
		endpointUtil = new EndpointUtil();
		nettyClient = new ConsumerAgentNettyClient(workerGroup);
//		nettyClient = new MockConsumerAgentNettyClient(workerGroup);

		httpServer = new HttpServer(bossGroup, workerGroup);
		httpServer.init();
	}

	public static HttpServer getHttpServer() {
		return httpServer;
	}

	public static ConsumerAgentNettyClient getNettyClient() {
		return nettyClient;
	}

	public static EndpointUtil getEndpointUtil() {
		return endpointUtil;
	}
}

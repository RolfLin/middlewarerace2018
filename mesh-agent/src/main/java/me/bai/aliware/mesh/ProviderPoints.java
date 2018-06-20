package me.bai.aliware.mesh;

import me.bai.aliware.mesh.netty.server.ProviderAgentNettyServer;
import me.bai.aliware.mesh.registry.EndpointUtil;

public class ProviderPoints {
	public static ProviderAgentNettyServer providerAgentNettyServer;
//	public static NettyDubboClient nettyDubboClient;

	public static void init() {
		EndpointUtil endpointUtil = new EndpointUtil();
//		nettyDubboClient = new NettyDubboClient();
		providerAgentNettyServer = new ProviderAgentNettyServer();
		providerAgentNettyServer.start();
	}
}

package me.bai.aliware.mesh.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

public class MyEtcdRegistry implements IRegistry {
	private final static Logger LOGGER = LoggerFactory.getLogger(MyEtcdRegistry.class);

	@Override
	public void register(String serviceName, int port) throws Exception {
	}

	@Override
	public List<Endpoint> find(String serviceName) throws Exception {
		List<Endpoint> list = new LinkedList<>();
		String ip = IpHelper.getHostIp();
		int port = Integer.valueOf(System.getProperty("provider.netty.server.port"));
		Endpoint e = new Endpoint(ip, port);
		e.setSize("small");
		list.add(e);
		LOGGER.info("Consumer模拟取出Etcd服务中的EndPoint--[{}:{}]", ip, port);
		return list;
	}
}

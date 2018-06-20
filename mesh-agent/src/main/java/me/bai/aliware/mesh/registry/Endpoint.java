package me.bai.aliware.mesh.registry;

import io.netty.channel.Channel;
import io.netty.channel.EventLoop;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class Endpoint {
	private final String host;
	private final int port;
	private Map<EventLoop, Channel> channelMap = new HashMap<>();
	private String size;

	public Endpoint(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String toString() {
		return host + ":" + port;
	}

	public boolean equals(Object o) {
		if (!(o instanceof Endpoint)) {
			return false;
		}
		Endpoint other = (Endpoint) o;
		return other.host.equals(this.host) && other.port == this.port;
	}

	public int hashCode() {
		return host.hashCode() + port;
	}

	private Random random = new Random();

	public Channel getChannel(EventLoop eventLoop) {
		return channelMap.get(eventLoop);
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}


	public void putChannel(EventLoop eventLoop, Channel channel) {
		channelMap.put(eventLoop, channel);
	}
}

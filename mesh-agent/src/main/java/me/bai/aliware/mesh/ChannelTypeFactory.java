package me.bai.aliware.mesh;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ChannelTypeFactory {
	private static boolean nio = System.getProperty("nio") != null;

	public static EventLoopGroup newEventLoopGroup() {
		return nio ? new NioEventLoopGroup() : new EpollEventLoopGroup();
	}

	public static EventLoopGroup newEventLoopGroup(int nThreads) {
		return nio ? new NioEventLoopGroup(nThreads) : new EpollEventLoopGroup(nThreads);
	}

	public static Class<? extends ServerSocketChannel> getServerSocketChannelClass() {
		return nio ? NioServerSocketChannel.class : EpollServerSocketChannel.class;
	}

	public static Class<? extends SocketChannel> getSocketChannelClass() {
		return nio ? NioSocketChannel.class : EpollSocketChannel.class;
	}
}

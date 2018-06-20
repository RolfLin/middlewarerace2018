package me.bai.aliware.mesh.nettyDubboClient;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import me.bai.aliware.mesh.netty.beans.NettyResponse;
import me.bai.aliware.mesh.nettyDubboClient.model.RpcResponse;
import me.bai.aliware.mesh.test.TimeRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyRpcClientHandler extends ChannelInboundHandlerAdapter {
	private static final Logger LOGGER = LoggerFactory.getLogger(MyRpcClientHandler.class);
	private Channel nettyChannel;

	public MyRpcClientHandler(Channel nettyChannel) {
		this.nettyChannel = nettyChannel;
	}

	@Override
	public void channelRead(ChannelHandlerContext channelHandlerContext, Object msg) {
		RpcResponse rpcResponse = (RpcResponse) msg;
		int httpReqId = (int) rpcResponse.getRequestId();
		NettyResponse nettyResponse = new NettyResponse(httpReqId, rpcResponse.getHash());
		nettyChannel.write(nettyResponse);
		if (LOGGER.isDebugEnabled()) LOGGER.debug("NettyServer已就绪response httpId:{}", httpReqId);
		TimeRecorder.mark(httpReqId, "ntyRes>>>");
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		nettyChannel.flush();
		if (LOGGER.isDebugEnabled()) LOGGER.debug("NettyServer flush");
	}
}

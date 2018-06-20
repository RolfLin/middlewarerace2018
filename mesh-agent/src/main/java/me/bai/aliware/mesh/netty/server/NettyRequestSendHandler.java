package me.bai.aliware.mesh.netty.server;

import io.netty.channel.*;
import me.bai.aliware.mesh.netty.beans.NettyRequest;
import me.bai.aliware.mesh.nettyDubboClient.NettyDubboClient;
import me.bai.aliware.mesh.nettyDubboClient.model.DubboRequest;
import me.bai.aliware.mesh.test.TimeRecorder;

public class NettyRequestSendHandler extends ChannelInboundHandlerAdapter {
	private Channel dubboChannel;

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		dubboChannel = NettyDubboClient.initChannel(ctx.channel());
		super.channelActive(ctx);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		NettyRequest nettyRequest = (NettyRequest) msg;
		int httpReqId = nettyRequest.getHttpReqId();
		DubboRequest dubboRequest = NettyDubboClient.createRequest(nettyRequest);
		TimeRecorder.mark(httpReqId, "dbo>>>");
		dubboChannel.writeAndFlush(dubboRequest).addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				TimeRecorder.mark(httpReqId, "dbo>>>ok");
			}
		});
	}

}

package me.bai.aliware.mesh.httpServer;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpContent;
import me.bai.aliware.mesh.netty.beans.NettyRequest;
import me.bai.aliware.mesh.test.TimeRecorder;

import java.util.concurrent.atomic.AtomicInteger;

public class HttpObjectToNettyRequestHandler extends ChannelInboundHandlerAdapter {
	private static final AtomicInteger httpReqCount = new AtomicInteger();

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof HttpContent) {
			int httpReqId = httpReqCount.incrementAndGet();
			TimeRecorder.mark(httpReqId, "http<<<");
			HttpContent httpContent = (HttpContent) msg;
			ByteBuf content = httpContent.content();
			NettyRequest request = new NettyRequest(httpReqId, content);
			ctx.fireChannelRead(request);
		}
	}
}

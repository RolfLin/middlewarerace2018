package me.bai.aliware.mesh.netty.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import me.bai.aliware.mesh.netty.beans.NettyRequest;
import me.bai.aliware.mesh.test.TimeRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettySendRequestHandler extends ChannelOutboundHandlerAdapter {
	private static final Logger LOGGER = LoggerFactory.getLogger(NettySendRequestHandler.class);

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		NettyRequest nettyRequest = (NettyRequest) msg;
		int httpReqId = nettyRequest.getHttpReqId();
		ByteBuf body = nettyRequest.getBody();
		ByteBuf header = ctx.alloc().directBuffer(6);
		int len = 6 + body.readableBytes();
		header.writeShort(len);
		header.writeInt(httpReqId);
		ctx.write(Unpooled.wrappedBuffer(header, body));
		TimeRecorder.mark(httpReqId, "ntyReq>>>");
		if (LOGGER.isDebugEnabled()) LOGGER.debug("netty 发送>>> {} ", httpReqId);
	}
}

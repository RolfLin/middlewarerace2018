package me.bai.aliware.mesh.netty.beans.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import me.bai.aliware.mesh.netty.beans.NettyResponse;
import me.bai.aliware.mesh.test.TimeRecorder;

public class NettyResponseEncoder extends MessageToByteEncoder<NettyResponse> {

	/*
	 * | int httpReqId | int hash |
	 */
	@Override
	protected void encode(ChannelHandlerContext ctx, NettyResponse response, ByteBuf out) throws Exception {
		out.writeInt(response.getHttpReqId());
		out.writeInt(response.getHash());
		TimeRecorder.end(response.getHttpReqId());
	}
}

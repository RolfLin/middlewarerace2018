package me.bai.aliware.mesh.netty.beans.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class NettyRequestEncoder extends MessageToByteEncoder {
	private int count;

	/*
	 * | short len | int httpReqId | byte[len-6] body |
	 */
	@Override
	protected void encode(ChannelHandlerContext ctx, Object request, ByteBuf out) throws Exception {
		count++;
		if (count >= 10) {
			ctx.flush();
			count = 0;
		}
//		byte[] body = request.getBody();
//		int httpReqId = request.getHttpReqId();
//		int len = 6 + body.length;
//		out.writeShort(len);
//		out.writeInt(httpReqId);
//		out.writeBytes(body);
	}

}

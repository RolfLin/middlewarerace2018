package me.bai.aliware.mesh.netty.beans.decoder;

import io.netty.buffer.ByteBuf;
import me.bai.aliware.mesh.DecodeResult;
import me.bai.aliware.mesh.netty.beans.NettyRequest;
import me.bai.aliware.mesh.test.TimeRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyRequestDecoder extends WhileReadableByteToMessageDecoder {
	private static final Logger LOGGER = LoggerFactory.getLogger(NettyRequestDecoder.class);

	/*
	 * | short len | int httpReqId | ByteBuf body(len-6) |
	 */
	protected Object decode2(ByteBuf in) {
		if (in.readableBytes() < 2) {
			return DecodeResult.NEED_MORE_INPUT;
		}
		int len = in.readShort();
		if (in.readableBytes() < len - 2) {
			return DecodeResult.NEED_MORE_INPUT;
		}
		int httpReqId = in.readInt();
		byte[] bs = new byte[len - 6];
		in.readBytes(bs);
		String bodyStr = new String(bs);
		NettyRequest nettyRequest = new NettyRequest(httpReqId, bodyStr);
		TimeRecorder.mark(httpReqId, "ntyReqDcd");
		return nettyRequest;
	}


}
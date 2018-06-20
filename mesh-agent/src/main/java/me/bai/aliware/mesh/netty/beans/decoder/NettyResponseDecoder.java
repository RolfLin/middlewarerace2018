package me.bai.aliware.mesh.netty.beans.decoder;

import io.netty.buffer.ByteBuf;
import me.bai.aliware.mesh.DecodeResult;
import me.bai.aliware.mesh.netty.beans.NettyResponse;
import me.bai.aliware.mesh.test.TimeRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyResponseDecoder extends WhileReadableByteToMessageDecoder {
	private static final Logger LOGGER = LoggerFactory.getLogger(NettyResponseDecoder.class);

	/*
	 * |int httpReqId |int hash |
	 */
	protected Object decode2(ByteBuf in) {
		if (in.readableBytes() < 8) {
			return DecodeResult.NEED_MORE_INPUT;
		}
		int httpReqId = in.readInt();
		int hash = in.readInt();
		NettyResponse nettyResponse = new NettyResponse(httpReqId,hash);
		TimeRecorder.mark(httpReqId, "ntyResDcd");
//		LOGGER.info("nettyResponse in decod2:{},id:{}", nettyResponse, nettyResponse.getHttpReqId());
		return nettyResponse;
	}

}

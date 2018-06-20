package me.bai.aliware.mesh.nettyDubboClient;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import me.bai.aliware.mesh.DecodeResult;
import me.bai.aliware.mesh.nettyDubboClient.model.RpcResponse;
import me.bai.aliware.mesh.test.TimeRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DubboRpcDecoder extends ByteToMessageDecoder {
	private static final int HEADER_LENGTH = 16;

	protected static final byte FLAG_EVENT = (byte) 0x20;
	protected static final Logger LOGGER = LoggerFactory.getLogger(DubboRpcDecoder.class);


	@Override
	protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> list) {
		long start = System.currentTimeMillis();

		while (in.isReadable()) {
			int savedReaderIndex = in.readerIndex();
			Object msg = decode2(in);
			if (msg == DecodeResult.NEED_MORE_INPUT) {
				in.readerIndex(savedReaderIndex);
				break;
			}
			if (msg != null) list.add(msg);
		}
		long end = System.currentTimeMillis();
		if (LOGGER.isDebugEnabled()) LOGGER.debug("dubbo 接收 {} dubboResponses cost:{}ms", list, end - start);
	}


	/**
	 * Demo为简单起见，直接从特定字节位开始读取了的返回值，demo未做：
	 * 1. 请求头判断
	 * 2. 返回值类型判断
	 *
	 * @param in input
	 * @return decoded RpcResponse
	 */
	private Object decode2(ByteBuf in) {
		int readable = in.readableBytes();

		if (readable < HEADER_LENGTH) {
			return DecodeResult.NEED_MORE_INPUT;
		}
		in.skipBytes(4);
		long requestId = in.readLong();
		TimeRecorder.mark((int) requestId,"dboDcd");
		int len = in.readInt();
		int tt = len + HEADER_LENGTH;
		if (readable < tt) {
			return DecodeResult.NEED_MORE_INPUT;
		}
		boolean win = in.getByte(in.readerIndex() + 2) == '\n';
		byte[] hashStringBytes;
		if (win) {
			hashStringBytes = new byte[len - 5];
			in.getBytes(in.readerIndex() + 3, hashStringBytes);
		} else {
			hashStringBytes = new byte[len - 3];
			in.getBytes(in.readerIndex() + 2, hashStringBytes);
		}
		String hashString = new String(hashStringBytes);
		int hash;
		try {
			hash = Integer.valueOf(hashString);
		} catch (NumberFormatException e) {
			return null;
		} finally {
			in.skipBytes(len);
		}
		RpcResponse response = new RpcResponse();
		response.setRequestId((requestId));
		response.setHash(hash);

		long dubboReqId = response.getRequestId();
		int httpReqId = (int) dubboReqId;

		return response;
	}
}

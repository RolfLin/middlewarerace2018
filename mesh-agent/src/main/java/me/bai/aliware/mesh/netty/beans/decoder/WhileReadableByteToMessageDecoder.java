package me.bai.aliware.mesh.netty.beans.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import me.bai.aliware.mesh.DecodeResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class WhileReadableByteToMessageDecoder extends ByteToMessageDecoder {
	private final static Logger LOGGER = LoggerFactory.getLogger(WhileReadableByteToMessageDecoder.class);

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
//		long start = System.currentTimeMillis();
		while (in.isReadable()) {
			int savedReaderIndex = in.readerIndex();
			Object msg = decode2(in);
			if (msg == DecodeResult.NEED_MORE_INPUT) {
				in.readerIndex(savedReaderIndex);
				break;
			}
			out.add(msg);
		}
//		long end = System.currentTimeMillis();
//		if (LOGGER.isDebugEnabled()) LOGGER.debug("netty 接收 {} cost:{}ms", out, end - start);
	}

	protected abstract Object decode2(ByteBuf in);
}

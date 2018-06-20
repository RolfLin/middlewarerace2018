package me.bai.aliware.mesh.nettyDubboClient;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import me.bai.aliware.mesh.nettyDubboClient.model.Bytes;
import me.bai.aliware.mesh.nettyDubboClient.model.DubboRequest;
import me.bai.aliware.mesh.nettyDubboClient.model.JsonUtils;
import me.bai.aliware.mesh.nettyDubboClient.model.RpcInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class DubboRpcEncoder extends MessageToByteEncoder {
	// header length.
	protected static final int HEADER_LENGTH = 16;
	// magic header.
	protected static final short MAGIC = (short) 0xdabb;
	// message flag.
	protected static final byte FLAG_REQUEST = (byte) 0x80;
	protected static final byte FLAG_TWOWAY = (byte) 0x40;
	protected static final byte FLAG_EVENT = (byte) 0x20;
	private final static Logger LOGGER = LoggerFactory.getLogger(DubboRpcEncoder.class);

	@Override
	protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf buffer) throws Exception {
		DubboRequest req = (DubboRequest) msg;

		// header.
		byte[] header = new byte[HEADER_LENGTH];
		// set magic number.
		Bytes.short2bytes(MAGIC, header);

		// set request and serialization flag.
		header[2] = (byte) (FLAG_REQUEST | 6);

		if (req.isTwoWay()) header[2] |= FLAG_TWOWAY;
		if (req.isEvent()) header[2] |= FLAG_EVENT;

		// set request id.
		Bytes.long2bytes(req.getId(), header, 4);

		// encode request data.
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		encodeRequestData(bos, req.getData());
		int len = bos.size();
		Bytes.int2bytes(len, header, 12);

		// write
		buffer.writeBytes(header);
		buffer.writeBytes(bos.toByteArray());
		if (LOGGER.isDebugEnabled()) LOGGER.debug("dubbo 发送 {} dubboResponses ", req.getId());

	}

	public void encodeRequestData(OutputStream out, Object data) throws Exception {
		RpcInvocation inv = (RpcInvocation) data;

		PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));

		JsonUtils.writeObject(inv.getAttachment("dubbo", "2.0.1"), writer);
		JsonUtils.writeObject(inv.getAttachment("path"), writer);
		JsonUtils.writeObject(inv.getAttachment("version"), writer);
		JsonUtils.writeObject(inv.getMethodName(), writer);
		JsonUtils.writeObject(inv.getParameterTypes(), writer);

		JsonUtils.writeBytes(inv.getArguments(), writer);
		JsonUtils.writeObject(inv.getAttachments(), writer);
	}

}

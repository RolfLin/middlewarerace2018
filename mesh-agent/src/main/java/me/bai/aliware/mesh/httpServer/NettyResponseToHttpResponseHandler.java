package me.bai.aliware.mesh.httpServer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.*;
import me.bai.aliware.mesh.netty.beans.NettyResponse;
import me.bai.aliware.mesh.test.TimeRecorder;

public class NettyResponseToHttpResponseHandler extends ChannelOutboundHandlerAdapter {

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		NettyResponse nettyResponse = (NettyResponse) msg;
		TimeRecorder.mark(nettyResponse.getHttpReqId(), "FHttpHdl");
		ByteBuf content = Unpooled.wrappedBuffer(String.valueOf(nettyResponse.getHash()).getBytes());
		//构造响应
		FullHttpResponse httpResponse =
				new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content);

		//设置要返回的内容长度
		httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes()); //内容长度
		TimeRecorder.end(nettyResponse.getHttpReqId());
		super.write(ctx, httpResponse, promise);
	}

}

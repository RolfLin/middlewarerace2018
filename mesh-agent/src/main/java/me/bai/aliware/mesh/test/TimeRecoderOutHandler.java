//package me.bai.aliware.mesh.test;
//
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.channel.ChannelOutboundHandlerAdapter;
//import io.netty.channel.ChannelPromise;
//
//public class TimeRecoderOutHandler extends ChannelOutboundHandlerAdapter {
//
//	private int id;
//	private boolean end;
//
//	public TimeRecoderOutHandler(int id) {
//		this(id, false);
//	}
//
//	public TimeRecoderOutHandler(int id, boolean end) {
//		this.id = id;
//		this.end = end;
//	}
//
//	@Override
//	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
//		if (end) {
//			TimeRecorder.end(id);
//		} else {
//			TimeRecorder.mark(id, "OutHdl");
//		}
//		super.write(ctx, msg, promise);
//	}
//}

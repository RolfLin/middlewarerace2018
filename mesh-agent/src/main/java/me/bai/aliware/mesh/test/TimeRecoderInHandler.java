//package me.bai.aliware.mesh.test;
//
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.channel.ChannelInboundHandlerAdapter;
//
//public class TimeRecoderInHandler extends ChannelInboundHandlerAdapter {
//	private int id;
//
//	public TimeRecoderInHandler(int id) {
//		this.id = id;
//	}
//
//	@Override
//	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//		TimeRecorder.mark(id, "InHdl");
//		super.channelRead(ctx, msg);
//	}
//}

//package me.bai.aliware.mesh.netty.client;
//
//
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.channel.ChannelInboundHandlerAdapter;
//import me.bai.aliware.mesh.ConsumerPoints;
//import me.bai.aliware.mesh.netty.beans.NettyResponse;
//
//public class NettyResponseToHttpServerHandler extends ChannelInboundHandlerAdapter {
//	@Override
//	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//		NettyResponse nettyResponse = (NettyResponse) msg;
//		ConsumerPoints.getHttpServer().response(nettyResponse.getHttpReqId(), msg);
//	}
//
//}

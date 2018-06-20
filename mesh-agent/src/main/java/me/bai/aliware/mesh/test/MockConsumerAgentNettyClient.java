//package com.alibaba.dubbo.performance.demo.agent.test;
//
//import ConsumerAgentNettyClient;
//import io.netty.buffer.ByteBuf;
//import io.netty.buffer.Unpooled;
//import io.netty.channel.EventLoop;
//import io.netty.channel.EventLoopGroup;
//import io.netty.channel.nio.NioEventLoop;
//import io.netty.utils.ReferenceCountUtil;
//
//public class MockConsumerAgentNettyClient extends ConsumerAgentNettyClient {
//
//	private static final int[] answers = new int[1024];
//
//	public MockConsumerAgentNettyClient(EventLoopGroup eventLoopGroup) {
//		super(eventLoopGroup);
//	}
//
//	public static void putAns(int httpReqId, int ans) {
//		answers[httpReqId % 1023] = ans;
//	}
//
//	public static int getAns(int httpReqId) {
//		return answers[httpReqId % 1023];
//	}
//
//	@Override
//	public void request(int httpReqId, ByteBuf p) {
//		byte[] bs = new byte[p.readableBytes()];
//		p.readBytes(bs);
//		ReferenceCountUtil.release(p);
//		putAns(httpReqId, new String(bs).hashCode());
//		super.request(httpReqId, Unpooled.wrappedBuffer(String.valueOf(httpReqId).getBytes()));
//	}
//
//	@Override
//	protected void response(int httpReqId, int hash) {
//		super.response(httpReqId, getAns(httpReqId));
//	}
//}

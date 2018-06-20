package me.bai.aliware.mesh.netty.beans;


import io.netty.buffer.ByteBuf;

public class NettyRequest {
	private int httpReqId;
	private ByteBuf body;
	private String bodyStr;


	public NettyRequest(int httpReqId, ByteBuf parameter) {
		this.httpReqId = httpReqId;
		this.body = parameter;
	}

	public NettyRequest(int httpReqId, String parameterStr) {
		this.httpReqId = httpReqId;
		this.bodyStr = parameterStr;
	}

	public int getHttpReqId() {
		return httpReqId;
	}

	public void setHttpReqId(int httpReqId) {
		this.httpReqId = httpReqId;
	}


	@Override
	public String toString() {
		return httpReqId + "";
	}

	public ByteBuf getBody() {
		return body;
	}

	public void setBody(ByteBuf body) {
		this.body = body;
	}

	public String getBodyStr() {
		return bodyStr;
	}

	public void setBodyStr(String bodyStr) {
		this.bodyStr = bodyStr;
	}
}

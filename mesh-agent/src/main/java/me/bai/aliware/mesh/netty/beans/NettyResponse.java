package me.bai.aliware.mesh.netty.beans;

public class NettyResponse {
	private int httpReqId;
	private int hash;

	public NettyResponse(int httpReqId, int hash) {
		this.httpReqId = httpReqId;
		this.hash = hash;
	}

	public int getHash() {
		return hash;
	}

	public int getHttpReqId() {
		return httpReqId;
	}

	public void setHttpReqId(int httpReqId) {
		this.httpReqId = httpReqId;
	}

	public void setHash(int hash) {
		this.hash = hash;
	}

//	@Override
//	public String toString() {
//		return "NettyResponse{" +
//				"httpReqId=" + httpReqId +
//				", hash=" + hash +
//				'}';
//	}

	@Override
	public String toString() {
		return httpReqId + "";
	}
}

package me.bai.aliware.mesh.nettyDubboClient.model;


public class RpcResponse {
	private long requestId;
	private int hash;

	public long getRequestId() {
		return requestId;
	}

	public void setRequestId(long requestId) {
		this.requestId = requestId;
	}

	public int getHash() {
		return hash;
	}

	public void setHash(int hash) {
		this.hash = hash;
	}

	@Override
	public String toString() {
		return requestId + "";
	}
}

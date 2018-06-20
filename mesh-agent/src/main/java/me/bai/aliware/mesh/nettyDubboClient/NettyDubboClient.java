package me.bai.aliware.mesh.nettyDubboClient;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.util.ReferenceCountUtil;
import me.bai.aliware.mesh.ChannelTypeFactory;
import me.bai.aliware.mesh.netty.beans.NettyRequest;
import me.bai.aliware.mesh.nettyDubboClient.model.DubboRequest;
import me.bai.aliware.mesh.nettyDubboClient.model.JsonUtils;
import me.bai.aliware.mesh.nettyDubboClient.model.RpcInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URLDecoder;

public class NettyDubboClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(NettyDubboClient.class);
	private static final int DUBBO_PORT = Integer.valueOf(System.getProperty("dubbo.protocol.port"));

	private static Bootstrap bootstrap = new Bootstrap()
			.option(ChannelOption.TCP_NODELAY, true)
			.channel(ChannelTypeFactory.getSocketChannelClass());

	public static Channel initChannel(Channel callbackChannel) {
		return bootstrap
				.clone(callbackChannel.eventLoop())
				.handler(new ChannelInitializer() {
					@Override
					protected void initChannel(Channel ch) throws Exception {
						//in
						ch.pipeline().addLast(new DubboRpcDecoder());
						ch.pipeline().addLast(new MyRpcClientHandler(callbackChannel));

						//out
						ch.pipeline().addLast(new DubboRpcEncoder());
					}
				})
				.connect("127.0.0.1", DUBBO_PORT)
				.channel();
	}

	//interface=com.alibaba.dubbo.performance.demo.provider.IHelloService&method=hash&parameterTypesString=Ljava%2Flang%2FString%3B&parameter=ldDFhwo5JSDa6kHNC9aKKouyLOKR8hIcluOgUkPMGppevxpjgfgpb9Arf9oEtAudAHtb6vnudNz03XRimNMDyxm00VcZYR5NKsxiNffzILwOiEKtMlsPHrI2dAw9M4kzSEVaaucr4BcOfWmVESrVOd0dbXN2E7sAhzg6XjLgETZYfMLX2G1IMbq3VNpnpPhRmMuJSpJ1qmiQAvsm7zQU9bfhGxngGslHcl1wfSN8I2ZIK9UxCjbzMKbcrganPVbS7lwQ3yzk67ZRfu9xcWAO6uwpcL39ZORQUtUjFVjwTSS1xMPMYAkl5exBk5kYFnzah9c23dFWCpzKUfgogTbtzSwT7jft23vhIWyabp5gZMkisFc1sxDsahErndkyC27iNzbNAXOfhLR6VHoFqHV7aypPISmX4hkrzjq
	public static DubboRequest createRequest(NettyRequest nettyRequest) throws UnsupportedEncodingException {
		int httpReqId = nettyRequest.getHttpReqId();
		String bodyStr = nettyRequest.getBodyStr();
		String decodedBodyStr = URLDecoder.decode(bodyStr, "UTF-8");
		String[] kvs = decodedBodyStr.split("&");
		String interfaceName = null;
		String method = null;
		String parameterTypesString = null;
		String parameter = null;
		for (String kv : kvs) {
			String[] ss = kv.split("=");
			String k = ss[0];
			String v = ss.length < 2 ? "" : ss[1];
			switch (k) {
				case "interface":
					interfaceName = v;
					break;
				case "method":
					method = v;
					break;
				case "parameterTypesString":
					parameterTypesString = v;
					break;
				case "parameter":
					parameter = v;
					break;
			}
		}
		return createRequest(httpReqId, interfaceName, method, parameterTypesString, parameter);
	}


	private static DubboRequest createRequest(int httpReqId, String interfaceName, String method, String parameterTypesString, String parameter) {
		RpcInvocation invocation = new RpcInvocation();
		invocation.setMethodName(method);
		invocation.setAttachment("path", interfaceName);
		invocation.setParameterTypes(parameterTypesString);    // Dubbo内部用"Ljava/lang/String"来表示参数类型是String
		invocation.setParameter(parameter);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));
		try {
			JsonUtils.writeObject(parameter, writer);
			ReferenceCountUtil.release(parameter);
		} catch (IOException e) {
			e.printStackTrace();
		}
		invocation.setArguments(out.toByteArray());

		DubboRequest dubboRequest = new DubboRequest((long) httpReqId);
		dubboRequest.setVersion("2.0.0");
		dubboRequest.setTwoWay(true);
		dubboRequest.setData(invocation);
		return dubboRequest;
	}
}

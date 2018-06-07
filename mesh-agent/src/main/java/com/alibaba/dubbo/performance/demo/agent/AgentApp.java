package com.alibaba.dubbo.performance.demo.agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AgentApp {
    // agent会作为sidecar，部署在每一个Provider和Consumer机器上
    // 在Provider端启动agent时，添加JVM参数-Dtype=provider -Dserver.port=30000 -Ddubbo.protocol.port=20889
    // 在Consumer端启动agent时，添加JVM参数-Dtype=consumer -Dserver.port=20000
    // 添加日志保存目录: -Dlogs.dir=/path/to/your/logs/dir。请安装自己的环境来设置日志目录。
//    private static IRegistry registry = new EtcdRegistry(System.getProperty("etcd.url"));
//    private static Logger logger = LoggerFactory.getLogger(AgentApp.class);
//    private static RpcClient rpcClient = new RpcClient(registry);
//    private static Integer providerPort = 30000;
    public static void main(String[] args) throws InterruptedException {
//        String type = System.getProperty("type");
//        if ("provider".equals(type)) {
//            logger.info("connection success!");
//            start();
////            return provider(interfaceName, method, parameterTypesString, parameter);
//        } else {
//            SpringApplication.run(AgentApp.class,args);
        SpringApplication.run(AgentApp.class,args);

    }

//    public static byte[] provider(String interfaceName, String method, String parameterTypesString, String parameter) throws Exception {
//
//        Object result = rpcClient.invoke(interfaceName, method, parameterTypesString, parameter);
//        return (byte[]) result;
//    }

//    public static void start() throws InterruptedException {
//        final ProviderServerHandler serverHandler = new ProviderServerHandler();
//        EventLoopGroup group = new NioEventLoopGroup();
//        try {
//            ServerBootstrap b = new ServerBootstrap();
//            b.group(group)
//                    .channel(NioServerSocketChannel.class)
//                    .localAddress(providerPort)
//                    .childHandler(new ChannelInitializer<SocketChannel>() {
//                        @Override
//                        protected void initChannel(SocketChannel ch) throws Exception {
//                            ch.pipeline().addLast(serverHandler);
//                        }
//                    });
//            ChannelFuture f = b.bind().sync();
//            f.channel().closeFuture().sync();
//        }finally {
//            group.shutdownGracefully().sync();
//        }
//    }

}

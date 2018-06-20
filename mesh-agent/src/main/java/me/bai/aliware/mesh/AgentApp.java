package me.bai.aliware.mesh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AgentApp {
	// agent会作为sidecar，部署在每一个Provider和Consumer机器上
	// 在Provider端启动agent时，添加JVM参数-Dtype=provider -Dserver.port=30000 -Ddubbo.protocol.port=20889
	// 在Consumer端启动agent时，添加JVM参数-Dtype=consumer -Dserver.port=20000
	// 添加日志保存目录: -Dlogs.dir=/path/to/your/logs/dir。请安装自己的环境来设置日志目录。

	public static void main(String[] args) throws Exception {
		SpringApplication.run(AgentApp.class, args);
		String type = System.getProperty("type");   // 获取type参数
		System.out.println("type:" + type);
		if ("consumer".equals(type)) {
			ConsumerPoints.init();
		} else if ("provider".equals(type)) {
			ProviderPoints.init();
		} else {
			System.out.println("Environment variable type is needed to set to provider or consumer.");
		}
	}
}

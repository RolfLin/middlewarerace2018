package me.bai.aliware.mesh.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;

import static me.bai.aliware.mesh.Constant.LOAD_BALANCE;

public class EndpointUtil {
	private final static Logger LOGGER = LoggerFactory.getLogger(EndpointUtil.class);
	private final Object lock = new Object();
	private IRegistry registry;
	private List<Endpoint> endpoints;
	private Random random = new Random();
	private int endpointNum;

	public EndpointUtil() {
		LOGGER.info("开始执行registry的初始化");
		String etcdUrl = System.getProperty("etcd.url");
//		if (etcdUrl != null) {
			registry = new EtcdRegistry(etcdUrl);
			LOGGER.info("传参进来的etcd地址：{}", etcdUrl);
//		} else {
//			registry = new MyEtcdRegistry();
//			LOGGER.info("采用模拟ETCD");
//		}
	}

	public Endpoint pickOneEndpoint() {
		return endpoints.get(random.nextInt(endpoints.size()));
	}

	private Endpoint small;
	private Endpoint medium;
	private Endpoint large;
	private int mediumLimit;
	private int largeLimit;
	private int totalLimit;

	{
		mediumLimit = LOAD_BALANCE[0] - 1;
		largeLimit = mediumLimit + LOAD_BALANCE[1];
		totalLimit = LOAD_BALANCE[0] + LOAD_BALANCE[1] + LOAD_BALANCE[2];
	}

	public Endpoint pickOneEndpoint(int n) {
		int mod = n % totalLimit;
		if (mod > largeLimit && endpointNum > 2) {
			return large;
		}
		if (mod > mediumLimit && endpointNum > 1) {
			return medium;
		} else {
			return small;
		}
	}

	public List<Endpoint> find() {
		if (endpoints == null) {
			synchronized (lock) {
				if (endpoints == null) {
					try {
						endpointNum = Integer.valueOf(System.getProperty("endpoint.num"));
						while (endpoints == null || endpoints.size() < endpointNum) {
							endpoints = registry.find("com.alibaba.dubbo.performance.demo.provider.IHelloService");
							Thread.sleep(1000);
						}
						for (Endpoint e : endpoints) {
							switch (e.getSize()) {
								case "large":
									large = e;
									break;
								case "medium":
									medium = e;
									break;
								default:
									small = e;
									break;
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					LOGGER.info("找到了endpoints：{}", endpoints);
				}
			}
		}
		return endpoints;
	}
}

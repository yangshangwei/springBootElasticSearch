package com.artisan.config;

import java.net.InetAddress;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ESConfig {

	private static final Logger logger = LoggerFactory.getLogger(ESConfig.class);

	@Value("${elasticsearch.ip}")
	private String hostName;

	@Value("${elasticsearch.port}")
	private String port;

	@Value("${elasticsearch.cluster.name}")
	private String clusterName;

	@Value("${elasticsearch.pool}")
	private String poolSize;

	@Bean
	public TransportClient transportClient()  {

		logger.info("Elasticsearch begin to init ");
		TransportClient transportClient = null;
		try {
			// 地址信息 
			TransportAddress transportAddress = new InetSocketTransportAddress(
					InetAddress.getByName(hostName), Integer.valueOf(port));
			
			// 配置信息
			Settings esSetting = Settings.builder().put("cluster.name", clusterName) // 集群名字
					.put("client.transport.sniff", true)// 增加嗅探机制，找到ES集群
					.put("thread_pool.search.size", Integer.parseInt(poolSize))// 线程池个数
					.build();
			
			// 配置信息Settings自定义
			transportClient = new PreBuiltTransportClient(esSetting);
			transportClient.addTransportAddresses(transportAddress);
		} catch (Exception e) {
			logger.error("TransportClient create error", e);
		}
		return transportClient;
	}
}
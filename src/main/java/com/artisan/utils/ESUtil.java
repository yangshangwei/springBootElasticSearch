package com.artisan.utils;

import javax.annotation.PostConstruct;

import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ESUtil {

	private static final Logger logger = LoggerFactory.getLogger(ESUtil.class);

	@Autowired
    private TransportClient transportClient;

    private static TransportClient client;

   
    @PostConstruct
    public void init() {
        client = this.transportClient;
    }

	/**
	 * 创建索引
	 *
	 * @param index
	 * @return
	 */

	public static boolean createIndex(String index) {
		if (!isIndexExist(index)) {
			logger.info("Index is not exits!");
			CreateIndexResponse indexresponse = client.admin().indices().prepareCreate(index).execute().actionGet();
			logger.info("执行建立成功？" + indexresponse.isAcknowledged());
			return indexresponse.isAcknowledged();
		}
		return false;
	}

	/**
	 * 删除索引
	 *
	 * @param index
	 * @return
	 */
	public static boolean deleteIndex(String index) {
		if (!isIndexExist(index)) {
			logger.info("Index is not exits!");
		}
		DeleteIndexResponse dResponse = client.admin().indices().prepareDelete(index).execute().actionGet();
		if (dResponse.isAcknowledged()) {
			logger.info("delete index " + index + "  successfully!");
		} else {
			logger.info("Fail to delete index " + index);
		}
		return dResponse.isAcknowledged();
	}

	/**
	 * 判断索引是否存在
	 *
	 * @param index
	 * @return
	 */
	public static boolean isIndexExist(String index) {
		IndicesExistsResponse inExistsResponse = client.admin().indices().exists(new IndicesExistsRequest(index))
				.actionGet();
		if (inExistsResponse.isExists()) {
			logger.info("Index [" + index + "] is exist!");
		} else {
			logger.info("Index [" + index + "] is not exist!");
		}
		return inExistsResponse.isExists();
	}

	/**
	 * 判断index下指定type是否存在
	 * 
	 * @param index
	 * @param type
	 * @return
	 */
	public static boolean isTypeExist(String index, String type) {
		return isIndexExist(index)
				? client.admin().indices().prepareTypesExists(index).setTypes(type).execute().actionGet().isExists()
				: false;
	}

}

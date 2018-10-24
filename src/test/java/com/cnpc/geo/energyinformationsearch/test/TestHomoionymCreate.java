package com.cnpc.geo.energyinformationsearch.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.cnpc.geo.energyinformationsearch.base.util.MD5Util;
import com.cnpc.geo.energyinformationsearch.es.client.ElasticSearchClientConfig;
import com.cnpc.geo.energyinformationsearch.es.client.ElasticSearchClientService;
import com.cnpc.geo.energyinformationsearch.es.template.QueryTemplate;

public class TestHomoionymCreate {
	static ElasticSearchClientService esclientService;
	static{
		ElasticSearchClientConfig clientConfig = new ElasticSearchClientConfig();
		clientConfig.setClusterName("elasticsearchtest");
		clientConfig.setNodeList("10.2.45.63:9300");
		clientConfig.setPingTimeOut("10s");
		clientConfig.setSniff(true);
		esclientService = new ElasticSearchClientService(clientConfig);
	}

	public static void main(String[] args) throws IOException {
		//读取同义词库文本
//		reda("F:/symdict.txt");
		
		//读取智能提示库文本
		requery("F:/mydict.txt");
	}
	
	
	public static void userQueryIndex(String userId, String sessionId, String keyWord) {

		String indexName = "query-chn-test";
		String type = "query";

		QueryTemplate queryTemplate = new QueryTemplate();
		queryTemplate.setKeyWord(keyWord);
		queryTemplate.setSessionId(sessionId);
		queryTemplate.setUserId(userId);
		queryTemplate.setCreateTime(System.currentTimeMillis());
		String indexJson = JSON.toJSONString(queryTemplate);
		String eid = MD5Util.MD5(keyWord);
		esclientService.createIndex(indexName, type, indexJson,eid);
	}
	
	
	
	/**
	 * 读取同义词库文本
	 * @throws IOException
	 */
	public static void reda(String filePath) throws IOException{
		File file = new File(filePath);
		BufferedReader reader = null;
		String consent = null;
		reader = new BufferedReader(new FileReader(file));
		while ((consent = reader.readLine()) != null) {
			mapconsent(consent);
		}
		reader.close();
	}
	/**
	 * 读取智能提示库文本
	 * @throws IOException
	 */
	public static void requery(String filePath) throws IOException{
		File file = new File(filePath);
		BufferedReader reader = null;
		String consent = null;
		reader = new BufferedReader(new FileReader(file));
		while ((consent = reader.readLine()) != null) {
			userQueryIndex("111", "333", consent.trim());
		}
		reader.close();
	}
	
	/**
	 * 拆分字符串形成key、value键值对
	 * @param consent
	 * @return
	 */
	
	private static void mapconsent(String consent){
		String key = null;
		String value = null;
		Map<String, String> map = new HashMap<String, String>();
		String replaceAll = consent.replaceAll("\\s", ",");
		String[] string = replaceAll.split(",");
		key = string[0];
		value = string[1];
		System.out.print("key=" + key);
		System.out.println("value=" + value);
		map.put("key", key);
		map.put("value", value);
		
		esclientService.createIndex("keyword-chn-2015", "keyword", JSON.toJSONString(map));
	}
}

package com.cnpc.geo.energyinformationsearch.base.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.cnpc.geo.energyinformationsearch.base.entity.Node;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

public class RedisUtil {
	
	private static Logger logger = Logger.getLogger(RedisUtil.class);

	private static JedisCluster cluster;

	public RedisUtil(List<Node> nodeList) {

		Set<HostAndPort> clusterNodes = new HashSet<HostAndPort>();

		for (Node node : nodeList) {
			clusterNodes.add(new HostAndPort(node.getIp(), node.getPort()));
		}

		cluster = new JedisCluster(clusterNodes);
	}

	/**
	 * redis入队
	 * @param key
	 * @param value
	 * @return 
	 */
	public static Long push(String key, String value) {
		Long lenth = null;
		try {
			lenth = cluster.rpush(key, value);
		} catch (Exception e) {
			logger.error("reids入队失败" + e.getMessage());
			e.printStackTrace();
		}
		return lenth;
	}

	/**
	 * redis出队
	 * @param key
	 * @return 
	 */
	public synchronized static String pop(String key) {
		String resultJson = null;
		try {
			resultJson = cluster.lpop(key);
		} catch (Exception e) {
			logger.error("reids出队失败" + e.getMessage());
			e.printStackTrace();
		}
		return resultJson;
	}

	
	/**
	 * redis根据下标查询
	 * @param key  队列名
	 * @param index  下标
	 */
	public static String getByIndex(String key, long index) {
		String result = null;
		try {
			result = cluster.lindex(key, index);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * redis查看列表长度
	 * @param key  队列名
	 */
	public static Long getLenth(String key) {
		Long lenth = null;
		try {
			lenth = cluster.llen(key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lenth;
	}
}

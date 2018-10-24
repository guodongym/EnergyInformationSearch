package com.cnpc.geo.energyinformationsearch.test;

import java.util.ArrayList;
import java.util.List;

import com.cnpc.geo.energyinformationsearch.base.entity.Node;
import com.cnpc.geo.energyinformationsearch.base.util.RedisUtil;

public class RedisTest {

	
	public static void main(String[] args) {
		List<Node> nodeList = new ArrayList<Node>();
		Node node = new Node();
		node.setIp("10.2.45.68");
		node.setPort(6379);
		nodeList.add(node);
		new RedisUtil(nodeList); 
		
//		System.out.println(RedisUtil.getLenth("synergyRecommend"));
//		System.out.println(RedisUtil.pop("queue_common"));
//		
		System.out.println(RedisUtil.getLenth("queue_common"));
		System.out.println(RedisUtil.getLenth("trs_task"));
		System.out.println(RedisUtil.getLenth("mail_task"));
		System.out.println(RedisUtil.getLenth("websiteURL"));
		
		
//		Request req = new Request();
//		req.setUrl("http://www.ce.cn");
//		req.setTaskId("aaa");
//		req.setDataSourceType(1);
//		req.setDataSourceName("bbb");
//		req.setMaxSeries(3);
//		req.setSelfDomin(false);
//		req.setSourceFormatType("1");
//		System.out.println(JSON.toJSONString(req));
//		
//		RedisUtil.push("queue_common", JSON.toJSONString(req));
//		
	}
}

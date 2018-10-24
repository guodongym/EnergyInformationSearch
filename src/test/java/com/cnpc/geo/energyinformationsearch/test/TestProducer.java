package com.cnpc.geo.energyinformationsearch.test;
import java.util.Properties;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

public class TestProducer {
	public static void main(String[] args) {
		try {
			Properties props = new Properties();
			props.put("zk.connect", "10.2.18.20:2181,10.2.18.24:2181");
			props.put("serializer.class", "kafka.serializer.StringEncoder");
			props.put("metadata.broker.list", "10.2.18.24:9093,10.2.18.20:9092");
			props.put("request.required.acks", "1");
			// props.put("partitioner.class", "com.xq.SimplePartitioner");
			ProducerConfig config = new ProducerConfig(props);
			Producer<String, String> producer = new Producer<String, String>(config);
			String msg = "this is a messageuuu!";
			KeyedMessage<String, String> data = new KeyedMessage<String, String>("shuai", msg);
			producer.send(data);
			System.out.println("发送成功!!!");
			producer.close();
		} catch (Exception e) {
			System.out.println("---------------");
			e.printStackTrace();
		}
	}

}
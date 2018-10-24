package com.cnpc.geo.energyinformationsearch.kafka;

import java.util.List;
import java.util.Map;


import kafka.consumer.KafkaStream;

public interface IConsumer<K,V> {


	/**
	 * 获取kakfa消息流集合，同时消费多个topic
	 * @param topics
	 * @param streamNum
	 * @return
	 */
	public Map<String, List<KafkaStream<K, V>>> getKafkaStreams(Map<String,Integer> topicStreams);
	/**
	 * 获取kakfa小溪流，
	 * @param topic 消费的topic名称
	 * @param streamNum 线程数
	 * @return
	 */
	public  List<KafkaStream<K, V>> getKafkaStream(String topic,Integer streamNum);
}

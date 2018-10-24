package com.cnpc.geo.energyinformationsearch.kafka;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

/**
 * 
 * @Package: consumer
 * @ClassName: KafkaConsumer
 * @Description: kafka的对象消费者，消费的对象类型根据生产者而定
 * @author liyongjun
 * @date 2015-4-23 上午11:21:37
 * @version V1.0
 * @ChangeHistoryList version modifier date description V1.0 liyongjun 2015-4-23
 *                    上午11:21:37
 */
public class KafkaConsumer {

	// 消費者實例
	private ConsumerConnector consumer;

	public KafkaConsumer(ConsumerConfig consumerConfig) {
		this.consumer = kafka.consumer.Consumer.createJavaConsumerConnector(consumerConfig);

	}

	public List<KafkaStream<byte[], byte[]>> getKafkaStream(String topic, Integer streamNum) {
		Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
		topicCountMap.put(topic, streamNum); // 描述读取哪个topic，需要几个线程读
		Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap); // 创建Streams

		List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic); // 每个线程对应于一个KafkaStream
		return streams;
	}

	public void shutdown() {
		if (consumer != null) {
			consumer.shutdown();
		}
	}

}

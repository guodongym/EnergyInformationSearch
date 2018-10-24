package com.cnpc.geo.energyinformationsearch.kafka;

import java.util.List;

public interface IProducer<V> {
	public void setMessage(V v);
	public void setMessages(List<V> v);

	/**
	 * 发送指定类型的数据给消息总线，通过k指定patition。如果k=null，则均匀分布在所有的patition上
	 * @param topic
	 * @param k
	 * @param v
	 */
	public void send(String target);
	/**
	 * 关闭连接
	 */
	public void close();
	public void send(String target,V v);
}

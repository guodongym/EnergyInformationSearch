package com.cnpc.geo.energyinformationsearch.kafka;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;


@Component 
public class KafkaProducer<V> implements IProducer<V>{

	@Autowired
	private Producer<String, V> producer;
	
	
	private List<V> messages=new ArrayList<V>();

	public List<V> getMessages() {
		return messages;
	}


	@Override
	public void send(String topic) {
		if (messages==null||messages.size()==0) {
			System.out.println("消息为空，发送失败");
			return;
		}
		List<KeyedMessage<String,V>> datas=new ArrayList<KeyedMessage<String,V>>();
		for (V v1:messages) {
			if (v1!=null) {
				KeyedMessage<String, V> data=new KeyedMessage<String, V>(topic,String.valueOf(new Date().getTime()),v1);
				datas.add(data);
			}
		}
		
		if (producer!=null) {
			producer.send(datas);
			messages.clear();
			System.out.println("发送成功");
		}else{
			throw new NullPointerException("producer is null- - - - - -");
		}

	}

	@Override
	public void close() {
		if (this.producer!=null) {
			producer.close();
			producer=null;
		}
	}

	@Override
	public void setMessage(V v) {
		if (messages==null) {
			messages=new ArrayList<V>();
		}
		messages.add(v);
	}

	@Override
	public void setMessages(List<V> v) {
		if (messages==null) {
			messages=new ArrayList<V>();
		}
		if (v==null||v.size()==0) {
			
			return;
		}
		messages.addAll(v);
	}

	@Override
	public void send(String target, V v) {
		// TODO Auto-generated method stub
		
	}

}

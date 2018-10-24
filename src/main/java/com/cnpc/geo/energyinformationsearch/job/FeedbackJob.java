package com.cnpc.geo.energyinformationsearch.job;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.springframework.beans.factory.InitializingBean;

import com.alibaba.fastjson.JSON;
import com.cnpc.geo.energyinformationsearch.base.util.SpringUtils;
import com.cnpc.geo.energyinformationsearch.httpclient.HttpClientUtil;
import com.cnpc.geo.energyinformationsearch.kafka.KafkaConsumer;

@DisallowConcurrentExecution
public class FeedbackJob implements InitializingBean, Runnable {
	Logger logger = Logger.getLogger(FeedbackJob.class);

	KafkaConsumer kafkaConsumer = SpringUtils.getBean("kafkaConsumer");

	/*
	 * @Override protected void executeInternal(JobExecutionContext arg0) throws
	 * JobExecutionException { // List<FeedbackMessage> messageList =
	 * executeConsumeMessage(); for(FeedbackMessage message : messageList){
	 * executeFeedbackMessage(message); } }
	 */

	public void executeConsumeMessage() {

		// List<FeedbackMessage> messageList = new ArrayList<FeedbackMessage>();
		List<KafkaStream<byte[], byte[]>> list = kafkaConsumer.getKafkaStream("fileFeedbackTopic", 1);

		ConsumerIterator<byte[], byte[]> it = list.get(0).iterator();
		while (it.hasNext()) {
			String messageJson = null;

			try {
				messageJson = new String(it.next().message(), "UTF-8");

				logger.info("messageJson:" + messageJson);
				System.out.println("FeedbackJson:" + messageJson);
			} catch (UnsupportedEncodingException e) {
				logger.error("", e);
			}

			FeedbackMessage message = JSON.parseObject(messageJson, FeedbackMessage.class);
			try {

				executeFeedbackMessage(message);

			} catch (Exception e) {
				logger.error(e);
			}
		}

	}

	/**
	 * 
	 * @Title: executeFeedbackMessage
	 * @author louyujie
	 * @param sid
	 * @param eid
	 * @param hid
	 * @param checkTag
	 * @param checkMsg
	 */
	public void executeFeedbackMessage(FeedbackMessage feedbackMessage) {
		String sid = feedbackMessage.getSid();
		String eid = feedbackMessage.getEid();
		String hid = feedbackMessage.getHid();
		String checkTag = feedbackMessage.getCheckTag();
		String checkMsg = feedbackMessage.getCheckMsg();
		logger.info("执行上传文件反馈任务，输入参数：sid：" + sid + "，eid：" + eid + "，hid:" + hid + ",checkTag:" + checkTag + ",checkMsg:" + checkMsg);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("sid", sid));
		params.add(new BasicNameValuePair("eid", eid));
		params.add(new BasicNameValuePair("hid", hid));
		params.add(new BasicNameValuePair("checkTag", checkTag));
		params.add(new BasicNameValuePair("checkMsg", checkMsg));
		UrlEncodedFormEntity uefEntity;
		try {
			URI url = new URI("http://10.2.45.102:10001/cnpc_geo/fileUpload/fileStatusFeedback.do");
			uefEntity = new UrlEncodedFormEntity(params, "UTF-8");
			HttpClientUtil.post(url, null, uefEntity);
		} catch (Exception e) {
			logger.info("executeFeedbackMessage--上传文件反馈任务，发送请求失败");
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		initialize();
	}

	public void initialize() {
		SingleExecutorUtil.execTask(this);
	}

	@Override
	public void run() {
		while (true) {
			try {
				executeConsumeMessage();
				Thread.sleep(1000);
			} catch (Exception e) {
				logger.error(e);
			}
		}
	}
}
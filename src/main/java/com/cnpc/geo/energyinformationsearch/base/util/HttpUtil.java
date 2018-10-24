package com.cnpc.geo.energyinformationsearch.base.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

public class HttpUtil {

	public static Logger logger = Logger.getLogger(HttpUtil.class);

	public static String connectPostHttps(String reqURL, Map<String, String> params) {

		String responseContent = null;

		HttpPost httpPost = new HttpPost(reqURL);
		try {
			CloseableHttpClient httpclient = HttpClients.createDefault();

			List<NameValuePair> formParams = new ArrayList<NameValuePair>();
			// 绑定到请求 Entry
			for (Map.Entry<String, String> entry : params.entrySet()) {
				formParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}
			httpPost.setEntity(new UrlEncodedFormEntity(formParams, Consts.UTF_8));

			CloseableHttpResponse response = httpclient.execute(httpPost);
			try {
				// 执行POST请求
				HttpEntity entity = response.getEntity(); // 获取响应实体
				try {
					if (null != entity) {
						responseContent = EntityUtils.toString(entity, Consts.UTF_8);
					}
				} finally {
					if (entity != null) {
						entity.getContent().close();
					}
				}
			} finally {
				if (response != null) {
					response.close();
				}
			}
			logger.info("requestURI : "+httpPost.getURI()+", responseContent: " + responseContent);
		} catch (ClientProtocolException e) {
			System.err.print("ClientProtocolException");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// 释放连接回连接池
			httpPost.releaseConnection();
		}
		return responseContent;

	}
}

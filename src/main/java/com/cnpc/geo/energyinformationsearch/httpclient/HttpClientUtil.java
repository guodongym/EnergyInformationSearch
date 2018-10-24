package com.cnpc.geo.energyinformationsearch.httpclient;

/**
 * 
 * @Package:com.cnpc_dlp.linuxmonitor.client
 * @Description:httpclient工具
 * @author:HZG
 * @Version：v1.0
 * @ChangeHistoryList：version     author         date              description 
 *                      v1.0        HZG    2014-9-17 下午8:27:09
 */
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

/**
 * Elemental example for executing multiple POST requests sequentially.
 */

public class HttpClientUtil {

	private static CloseableHttpClient httpClient;
	public final static int connectTimeout = 10000;
	static {
		// 获取HttpClient实例
		httpClient = HttpClientFactory.getInstance();
	}

	/**
	 * 
	 * @Description：Get方法，携带请求头部信息
	 * @param url
	 * @param headerList
	 * @throws URISyntaxException
	 * @throws ClientProtocolException
	 * @throws IOException
	 *             void
	 * @author:quwu
	 * @Date :2014-10-27 上午10:31:11
	 */
	public static void get(URI url, Header[] headerList) throws URISyntaxException, ClientProtocolException, IOException {

		HttpGet get = new HttpGet();

		get.setURI(url);
		if (headerList != null)
			for (Header header : headerList) {
				get.addHeader(header);
			}

		long start = System.currentTimeMillis();
		System.out.println("开始时间:" + start);
		CloseableHttpResponse response = httpClient.execute(get);
		long jiange = (System.currentTimeMillis() - start) / 1000;
		System.out.println("结束时间:" + jiange);
		HttpEntity entity = response.getEntity();
		String str = EntityUtils.toString(entity, "UTF-8");
		System.out.println(str);
	}

	/**
	 * 
	 * @Description：POST方法，携带请求头部头部，请求实体信息
	 * @param url
	 * @param headerList
	 * @param requestBody
	 * @throws URISyntaxException
	 * @throws ClientProtocolException
	 * @throws IOException
	 *             void
	 * @author:quwu
	 * @Date :2014-10-27 上午10:30:30
	 */
	public static void post(URI url, Header[] headerList, StringEntity requestBody) throws URISyntaxException, ClientProtocolException, IOException {

		HttpPost post = new HttpPost();

		post.setURI(url);

		if (headerList != null)
			for (Header header : headerList) {
				post.addHeader(header);
			}
		if (requestBody != null)
			post.setEntity(requestBody);

		long start = System.currentTimeMillis();
		System.out.println("开始时间:" + start);
		CloseableHttpResponse response = httpClient.execute(post);
		long jiange = (System.currentTimeMillis() - start) / 1000;
		System.out.println("结束时间:" + jiange);
		HttpEntity entity = response.getEntity();
		String str = EntityUtils.toString(entity, "UTF-8");
		System.out.println(str);
	}

	/**
	 * 
	 * @Description：POST方法,携带请求实体信息
	 * @param reqURL
	 * @param params
	 * @return String
	 * @author:quwu
	 * @Date :2014-10-27 上午10:28:49
	 */
	public static String connectPostHttps(String reqURL, Map<String, String> params) {

		String responseContent = null;

		HttpPost httpPost = new HttpPost(reqURL);
		try {
			RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(connectTimeout).setConnectTimeout(connectTimeout).setConnectionRequestTimeout(connectTimeout).build();

			List<NameValuePair> formParams = new ArrayList<NameValuePair>();
			// 绑定到请求 Entry
			for (Map.Entry<String, String> entry : params.entrySet()) {
				formParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}
			httpPost.setEntity(new UrlEncodedFormEntity(formParams, Consts.UTF_8));
			httpPost.setConfig(requestConfig);

			CloseableHttpResponse response = httpClient.execute(httpPost);
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
			System.out.println("requestURI : " + httpPost.getURI() + ", responseContent: " + responseContent);
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

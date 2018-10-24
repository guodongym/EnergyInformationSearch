package com.cnpc.geo.energyinformationsearch.httpclient;
/**
 * 
 * @Package:com.cnpc_dlp.linuxmonitor.client
 * @Description:httpclient工厂
 * @author:quwu
 * @Version：v1.0
 * @ChangeHistoryList：version     author         date              description 
 *                      v1.0        quwu    2014-9-17 下午8:27:09
 */
import java.util.ArrayList;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;


public class HttpClientFactory {
	/**
	 * 
	 * @Description：获取HttpClient实例
	 * @return
	 * CloseableHttpClient
	 * @author:quwu
	 * @Date :2014-10-27 上午10:23:57
	 */
	public static CloseableHttpClient getInstance() {
		
		HttpClientBuilder builder = HttpClients.custom();
		
		//实例化连接保持模板
		ConnectionKeepAliveStrategy keepAliveStrat = new ConnectionKeepAliveStrategy() {
    	    public long getKeepAliveDuration(
    	            HttpResponse response,
    	            HttpContext context) {
    	    	 // Honor 'keep-alive' header
    	        HeaderElementIterator it = new BasicHeaderElementIterator(
    	                response.headerIterator(HTTP.CONN_KEEP_ALIVE));
    	        while (it.hasNext()) {
    	            HeaderElement he = it.nextElement();
    	            String param = he.getName();
    	            String value = he.getValue();
    	            if (value != null && param.equalsIgnoreCase("timeout")) {
    	                try {
    	                    return Long.parseLong(value) * 1000;
    	                } catch(NumberFormatException ignore) {
    	                }
    	            }
    	        }
    	        HttpHost target = (HttpHost) context.getAttribute(
    	                HttpClientContext.HTTP_TARGET_HOST);
    	        if ("www.naughty-server.com".equalsIgnoreCase(target.getHostName())) {
    	            // Keep alive for 5 seconds only
    	            return 5 * 1000;
    	        } else {
    	            // otherwise keep alive for 30 seconds
    	            return 30 * 1000;
    	        }
    	    }
    	};
    	
    	
    	ArrayList<Header> headers = new ArrayList<Header>();
    	Header header = new BasicHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:30.0) Gecko/20100101 Firefox/30.0");
    	headers.add(header);
    	//设置连接保持策略
    	builder.setKeepAliveStrategy(keepAliveStrat);
    	//设置连接管理器
    	builder .setConnectionManager(HttpConnectionManagerFactory.getClientConnectionManager());
    	//设置默认发送Http 头部信息
    	builder.setDefaultHeaders(headers);
		
		return builder.build();
		
	}

}

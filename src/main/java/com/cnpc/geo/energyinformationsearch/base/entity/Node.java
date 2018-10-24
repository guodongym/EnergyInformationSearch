package com.cnpc.geo.energyinformationsearch.base.entity;
/**
 * 
 * @Package: com.cnpc.geo.crawlmaster
 * @ClassName: Node
 * @Description: 物理机结点
 * @author quwu
 * @date 2015年4月10日 下午5:42:52
 * @version V1.0
 * @ChangeHistoryList    version    modifier        date                description
 *                        V1.0        quwu     2015年4月10日 下午5:42:52
 */
public class Node {
	private String ip;
	private Integer port;
	
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	
	public Integer getPort() {
		return port;
	}
	
	public void setPort(Integer port) {
		this.port = port;
	}

}

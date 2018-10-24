package com.cnpc.geo.energyinformationsearch.es.client;

/**
 * 
 * @Package: com.cnpc.geo.energyinformationsearch.es
 * @ClassName: ElasticSearchClientConfig
 * @author quwu
 * @date 2015年7月1日 上午9:35:32
 * @version V1.0
 * @ChangeHistoryList version modifier date description V1.0 quwu 2015年7月1日
 *                    上午9:35:32
 */
public class ElasticSearchClientConfig {

	private String nodeList; // 结点集合
	private String clusterName; // 集群名
	private Boolean sniff; // 嗅探
	private String pingTimeOut;

	public String getNodeList() {
		return nodeList;
	}

	public void setNodeList(String nodeList) {
		this.nodeList = nodeList;
	}

	public Boolean getSniff() {
		return sniff;
	}

	public void setSniff(Boolean sniff) {
		this.sniff = sniff;
	}

	public String getPingTimeOut() {
		return pingTimeOut;
	}

	public void setPingTimeOut(String pingTimeOut) {
		this.pingTimeOut = pingTimeOut;
	}

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}
}
package com.cnpc.geo.energyinformationsearch.hbase.client;

/**
 * 
 * @Package: com.cnpc.geo.energyinformationsearch.hbase
 * @ClassName: HbaseClientConfig
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author quwu
 * @date 2015年7月1日 下午5:48:20
 * @version V1.0
 * @ChangeHistoryList    version    modifier        date                description
 *                        V1.0        quwu     2015年7月1日 下午5:48:20
 */
public class HbaseClientConfig {
	
	private String hbaseZookeeperQuorum;
	
	private String hbaseMaster;
	
	private String zookeeperClientPort;
	
	private String zookeeperZnodeParent;

	public String getHbaseZookeeperQuorum() {
		return hbaseZookeeperQuorum;
	}

	public void setHbaseZookeeperQuorum(String hbaseZookeeperQuorum) {
		this.hbaseZookeeperQuorum = hbaseZookeeperQuorum;
	}

	public String getHbaseMaster() {
		return hbaseMaster;
	}

	public void setHbaseMaster(String hbaseMaster) {
		this.hbaseMaster = hbaseMaster;
	}

	public String getZookeeperClientPort() {
		return zookeeperClientPort;
	}

	public void setZookeeperClientPort(String zookeeperClientPort) {
		this.zookeeperClientPort = zookeeperClientPort;
	}

	public String getZookeeperZnodeParent() {
		return zookeeperZnodeParent;
	}

	public void setZookeeperZnodeParent(String zookeeperZnodeParent) {
		this.zookeeperZnodeParent = zookeeperZnodeParent;
	}
	
}

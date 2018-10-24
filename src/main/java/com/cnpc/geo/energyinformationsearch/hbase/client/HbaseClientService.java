package com.cnpc.geo.energyinformationsearch.hbase.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

/**
 * 
 * @Package: com.cnpc.geo.energyinformationsearch.hbase
 * @ClassName: HbaseClientService
 * @author quwu
 * @date 2015年7月2日 下午1:20:00
 * @version V1.0
 * @ChangeHistoryList version modifier date description V1.0 quwu 2015年7月2日
 *                    下午1:20:00
 */
public class HbaseClientService {

	Logger logger = Logger.getLogger(HbaseClientService.class);

	private Configuration conf;

	/**
	 * 
	 * @author quwu
	 * @param clientConfig
	 */
	public HbaseClientService(HbaseClientConfig clientConfig) {
		conf = HBaseConfiguration.create();
		conf.set("hbase.master", clientConfig.getHbaseMaster());
		conf.set("hbase.zookeeper.property.clientPort", clientConfig.getZookeeperClientPort());
		conf.set("hbase.zookeeper.quorum", clientConfig.getHbaseZookeeperQuorum());
		conf.set("zookeeper.znode.parent", clientConfig.getZookeeperZnodeParent());
	}

	/**
	 * 
	 * @Title: getConnection
	 * @author quwu
	 * @return
	 */
	public HConnection getConnection() {
		HConnection hConnection = null;
		try {
			hConnection = HConnectionManager.createConnection(conf);
		} catch (IOException e) {
			logger.error("/HbaseClientService/getConnection()-->", e);
		}

		return hConnection;
	}

	/**
	 * 
	 * @Title: close
	 * @author quwu
	 * @param hConnection
	 */
	public void close(HConnection hConnection) {
		try {
			hConnection.close();
		} catch (IOException e) {
			logger.error("/HbaseClientService/close()-->", e);
		}
	}

	/**
	 * 
	 * @Title: createTable
	 * @author quwu
	 * @param tableName
	 * @param cfs
	 */
	@SuppressWarnings("deprecation")
	public void createTable(String tableName, String[] cfs) {
		HBaseAdmin admin;
		try {

			admin = new HBaseAdmin(conf);

			if (admin.tableExists(tableName)) {

				admin.disableTable(tableName);
				admin.deleteTable(tableName);
				System.out.println(tableName + " is exist,detele....");

			} else {

				HTableDescriptor tableDesc = new HTableDescriptor(tableName);
				for (int i = 0; i < cfs.length; i++) {
					tableDesc.addFamily(new HColumnDescriptor(cfs[i]));
				}
				admin.createTable(tableDesc);

				System.out.println("表创建成功！");
			}
			admin.close();

		} catch (IOException e) {
			logger.error("/HbaseClientService/createTable()-->", e);
		}
	}

	/**
	 * 
	 * @Title: deleteTable
	 * @author quwu
	 * @param tablename
	 * @throws IOException
	 */
	public void deleteTable(String tableName) {
		try {
			HBaseAdmin admin = new HBaseAdmin(conf);
			admin.disableTable(tableName);
			admin.deleteTable(tableName);

			System.out.println("表删除成功！");
			admin.close();
		} catch (MasterNotRunningException e) {
			logger.error("/HbaseClientService/deleteTable()-->", e);
		} catch (ZooKeeperConnectionException e) {
			logger.error("/HbaseClientService/deleteTable()-->", e);
		} catch (IOException e) {
			logger.error("/HbaseClientService/deleteTable()-->", e);
		}

	}

	/**
	 * 
	 * @Title: writeRow
	 * @author quwu
	 * @param tableName
	 * @param rowkey
	 * @param map
	 */
	public void writeRow(String tableName, String rowKey, Map<String, Object> map) {
		try {

			HConnection hConnection = getConnection();
			HTableInterface table = hConnection.getTable(tableName);

			Put put = new Put(Bytes.toBytes(rowKey));
			Iterator<String> iterator = map.keySet().iterator();

			while (iterator.hasNext()) {
				String keyString = (String) iterator.next();
				String[] tmp = keyString.split(":");
				put.add(Bytes.toBytes(tmp[0]), Bytes.toBytes(tmp[1]), Bytes.toBytes(map.get(keyString).toString()));
				table.put(put);
			}

			table.close();
			this.close(hConnection);

		} catch (IOException e) {
			logger.error("/HbaseClientService/writeRow()-->", e);
		}

	}

	/**
	 * 
	 * @Title: deleteRow
	 * @author quwu
	 * @param tableName
	 * @param rowKey
	 */
	public void deleteRow(String tableName, String rowKey) {
		HConnection hConnection = getConnection();
		HTableInterface table;
		try {
			table = hConnection.getTable(tableName);

			List<Delete> list = new ArrayList<Delete>();
			Delete d1 = new Delete(rowKey.getBytes());
			list.add(d1);
			table.delete(list);
			System.out.println("删除行成功！");
			table.close();

		} catch (IOException e) {
			logger.error("/HbaseClientService/deleteRow()-->", e);
		}

		this.close(hConnection);
	}

	/**
	 * 
	 * @Title: selectRow
	 * @author quwu
	 * @param tableName
	 * @param rowKey
	 * @return
	 */
	public Result selectRow(String tableName, String rowKey) {
		HConnection hConnection = getConnection();
		HTableInterface table;
		Result rs = null;
		try {

			table = hConnection.getTable(tableName);
			Get g = new Get(rowKey.getBytes());
			rs = table.get(g);
			table.close();

		} catch (IOException e) {
			logger.error("/HbaseClientService/selectRow()-->", e);
		}

		this.close(hConnection);

		return rs;
	}

	/**
	 * 
	 * @Title: selectRowAddColumns
	 * @author quwu
	 * @param tableName
	 * @param rowKey
	 * @param set
	 * @return
	 */
	public Result selectRowAddColumns(String tableName, String rowKey, Set<String> set) {
		Result rs = null;

		try {

			HConnection hConnection = getConnection();
			HTableInterface table = hConnection.getTable(tableName);

			Get g = new Get(rowKey.getBytes());

			Iterator<String> iterator = set.iterator();

			while (iterator.hasNext()) {
				String str = (String) iterator.next();
				String[] tmp = str.split(":");
				g.addColumn(Bytes.toBytes(tmp[0]), Bytes.toBytes(tmp[1]));
			}

			rs = table.get(g);
			table.close();
			this.close(hConnection);

		} catch (IOException e) {
			logger.error("/HbaseClientService/selectRowAddColumns()-->", e);
		}

		return rs;
	}

	/**
	 * 
	 * @Title: scaner
	 * @author quwu
	 * @param tableName
	 * @return
	 */
	public ResultScanner scaner(String tableName) {
		HConnection hConnection = getConnection();
		HTableInterface table;
		ResultScanner rs = null;
		try {
			table = hConnection.getTable(tableName);
			Scan s = new Scan();
			rs = table.getScanner(s);
			table.close();
		} catch (IOException e) {
			logger.error("/HbaseClientService/scaner()-->", e);
		}

		this.close(hConnection);
		return rs;
	}

	public void updateHbase(String tableName, String hId, String family, String qualifier, long value) {
		logger.info("public void updateHbase(String tableName,String hId,String family,String qualifier,long value){");
		HConnection hConnection = getConnection();
		HTableInterface table;
		try {
			table = hConnection.getTable(tableName);
			Put updateb = new Put(new String(hId).getBytes());
			updateb.add(family.getBytes(), qualifier.getBytes(), String.valueOf(value).getBytes());
			table.put(updateb);
			table.flushCommits();
		} catch (IOException e) {
			logger.error("public void updateHbase(String tableName,String hId,String family,String qualifier,long value){" + e);
			e.printStackTrace();
		}

	}

	public static void main(String[] args) throws IOException {

		HbaseClientConfig clientConfig = new HbaseClientConfig();
		clientConfig.setHbaseMaster("hdp8.cnpc-ggo.com");
		clientConfig.setZookeeperClientPort("2181");
		clientConfig.setHbaseZookeeperQuorum("hdp7.cnpc-ggo.com,hdp8.cnpc-ggo.com,hdp9.cnpc-ggo.com,hdp10.cnpc-ggo.com,hdp11.cnpc-ggo.com");
		clientConfig.setZookeeperZnodeParent("/hbase-unsecure");

		HbaseClientService clientService = new HbaseClientService(clientConfig);
		String[] cfs = new String[1];
		cfs[0] = "t1";

		clientService.createTable("test", cfs);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("t1:name", "zhangsan");

		clientService.writeRow("test", "123", map);

		Set<String> set = new HashSet<String>();
		set.add("t1:name");
		Result rs = clientService.selectRowAddColumns("test", "123", set);
		System.out.println(new String(rs.getValue(Bytes.toBytes("t1"), Bytes.toBytes("name"))));

	}

}

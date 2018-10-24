package com.cnpc.geo.energyinformationsearch.httpclient;

/**
 * 
 * @Package:com.cnpc_dlp.linuxmonitor.main
 * @Description:系统配置类
 * @author:quwu
 * @Version：v1.0
 * @ChangeHistoryList：version     author         date              description 
 *                      v1.0        quwu    2014-9-17 下午8:09:42
 */
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

public class ConfigInfo {

	private static Logger log = Logger.getLogger(ConfigInfo.class.getName());
	// 服务器ID
	private static Integer serverId;
	// 服务器类型 1：监测器，2：平台，3：数据库
	private static Integer serverType;
	// 系统时间不一致标记
	private static Boolean timeBugFlag;
	// 系统时间不一致描述
	private static String timeBugNote;
	// 密码
	private static String passwd;
	// 命令码
	private static String command;
	// 告警代码
	private static String code;
	// 用来恢复keystore中密钥的密码
	private static String keyPassword;
	// 用来检验truststore完整性、解锁keystore的密码
	private static String trustPassword;
	// quartz crontab表达式
	private static String cronSysExpression;
	// keyStore路径
	private static String keyStorePath;
	// trustStore路径
	private static String trustStorePath;
	// 代理心跳接口url
	private static String heartBeatUrl;
	// 时间告警接口url
	private static String alarmReportIfaceUrl;
	// 出错时间最大间隔（分钟）
	private static Integer wrongTimeMaxInterval;
	// 时间告警轮询间隔(秒)
	private static Integer alarmTimeInterval;
	// ntp服务器ip
	private static String ntpHostAddr;
	// ntp开关
	private static Boolean ntpSwitch;
	// 硬盘分区列表
	private static Set<String> fileSystemes = new HashSet<String>();

	public static Set<String> getFileSystemes() {
		return fileSystemes;
	}

	public static void setTimeBugFlag(Boolean timeBugFlag) {
		ConfigInfo.timeBugFlag = timeBugFlag;
	}

	public static void setTimeBugNote(String timeBugNote) {
		ConfigInfo.timeBugNote = timeBugNote;
	}

	public static Boolean getNtpSwitch() {
		return ntpSwitch;
	}

	public static String getNtpHostAddr() {
		return ntpHostAddr;
	}

	public static Integer getWrongTimeMaxInterval() {
		return wrongTimeMaxInterval;
	}

	public static String getAlarmReportIfaceUrl() {
		return alarmReportIfaceUrl;
	}

	public static String getHeartBeatUrl() {
		return heartBeatUrl;
	}

	public static String getCronSysExpression() {
		return cronSysExpression;
	}

	public static String getPasswd() {
		return passwd;
	}

	public static Integer getAlarmTimeInterval() {
		return alarmTimeInterval;
	}

	public static String getCommand() {
		return command;
	}

	public static String getCode() {
		return code;
	}

	public static String getTimeBugNote() {
		return timeBugNote;
	}

	public static Boolean getTimeBugFlag() {
		return timeBugFlag;
	}

	public static Integer getServerId() {
		return serverId;
	}

	public static Integer getServerType() {
		return serverType;
	}

	public static String getKeyPassword() {
		return keyPassword;
	}

	public static String getTrustPassword() {
		return trustPassword;
	}

	public static String getKeyStorePath() {
		return keyStorePath;
	}

	public static String getTrustStorePath() {
		return trustStorePath;
	}

	/**
	 * 
	 * @Description：初始化方法 void
	 * @author:quwu
	 * @Date :2014-9-17 下午8:14:38
	 */
	public static void init() {
		// 初始化服务器相关参数
		Properties prop = new Properties();
		String file = System.getProperty("user.dir") + "/config/server.properties";

		InputStream in;
		try {
			in = new FileInputStream(file);
			prop.load(in);
			// 服务器id
			serverId = Integer.valueOf(prop.getProperty("serverId").trim());
			// 服务器类型
			serverType = Integer.valueOf(prop.getProperty("serverType").trim());
			// 密码
			passwd = prop.getProperty("passwd").trim();
			// 命令码
			command = prop.getProperty("command").trim();
			// 告警代码
			code = prop.getProperty("code").trim();

			/*
			 * //DESUtil des = new DESUtil(
			 * "8D1F65EF70008D67340A76F653A94664055B3CE3C9262D96" );
			 * //用来恢复keystore中密钥的密码 keyPassword =
			 * prop.getProperty("keyPassword").trim(); keyPassword =
			 * des.decryptStr(keyPassword);
			 * 
			 * //用来检验truststore完整性、解锁keystore的密码 trustPassword =
			 * prop.getProperty("trustPassword").trim(); trustPassword =
			 * des.decryptStr(trustPassword);
			 */
			// quartz crontab表达式
			cronSysExpression = prop.getProperty("cronSysExpression").trim();
			// keyStore路径
			keyStorePath = prop.getProperty("keyStorePath").trim();
			// trustStore路径
			trustStorePath = prop.getProperty("trustStorePath").trim();
			// 代理心跳接口url
			heartBeatUrl = prop.getProperty("heartBeatUrl").trim();
			// 时间告警接口url
			alarmReportIfaceUrl = prop.getProperty("alarmReportIfaceUrl").trim();
			// 出错时间最大间隔（分钟）
			wrongTimeMaxInterval = Integer.valueOf(prop.getProperty("wrongTimeMaxInterval").trim());
			// 时间告警间隔（秒）
			alarmTimeInterval = Integer.valueOf(prop.getProperty("alarmTimeInterval").trim());
			// ntp服务器ip
			ntpHostAddr = prop.getProperty("ntpHostAddr").trim();
			// ntp开关
			ntpSwitch = Boolean.valueOf(prop.getProperty("ntpSwitch").trim());
			// 分区列表
			String[] fileSystemList = prop.getProperty("fenQuList").split(",");
			for (String fileSystem : fileSystemList) {
				fileSystemes.add(fileSystem);
			}

		} catch (IOException e) {
			log.error(e);
		}

		// 初始化时间不一致参数
		String timeFile = System.getProperty("user.dir") + "/persistence/Time.txt";
		BufferedReader scan = null;
		try {
			scan = new BufferedReader(new FileReader(timeFile));
			try {
				timeBugNote = scan.readLine();
				if (timeBugNote != null) {
					timeBugFlag = true;
				} else {
					timeBugFlag = false;
				}
				log.info("Time.txt is :" + timeBugNote);

			} catch (IOException e) {
				log.error(e);
			}
		} catch (FileNotFoundException e) {
			log.error(e);
		}
	}
}
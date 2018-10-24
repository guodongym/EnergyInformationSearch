package com.cnpc.geo.energyinformationsearch.base.entity;

/**
 * @Package: com.cnpc.geo.crawlmaster.base.util
 * @ClassName: CommonParameters
 * @Description: TODO 公共的参数都写在这里，比如成功状态码等
 * @author louyujie
 * @date 2015年4月21日 上午9:47:07
 * @version V1.0
 * @ChangeHistoryList version modifier date description V1.0 louyujie 2015年4月21日
 *                    上午9:47:07
 */

public class CommonParameters {
	// 成功状态码
	public static final String resultCode_OK = "200";
	// 失败状态码
	public static final String resultCode_ERROR = "201";
	// 任务状态码为1代表正在执行中
	public static final String status_1 = "1";
	// 任务状态码为1代表已完成
	public static final String status_2 = "2";
	// 任务状态码为3代表已删除
	public static final String status_3 = "3";

	public final static int SUCCESSCODE = 200;

	public final static int ERRORCODE = 201;

	public final static String SUCCESSMEG = "操作成功";

	public final static String ERRORMEG = "操作失败";

	public final static String IPAddRESS = "127.0.0.1";

	public final static int PROT = 9091;

	public final static int TIMEOUT = 6000;

	public final static String KEYSTOREPATH = "WEB-INF/thrift/keyStore.jks";

	public final static String KEYSTOREPASS = "123456";

	public final static int MAXPOOLSIZE = 100;

	public final static int MINPOOLSIZE = 5;

	public final static String previwe = "01";

	public final static String upload = "02";

	public final static String download = "03";

	public final static String LOCK = "1";

	public final static String UN_LOCK = "2";

	public final static String EQUAL = "1";

	public final static String UN_EQUAL = "2";

}

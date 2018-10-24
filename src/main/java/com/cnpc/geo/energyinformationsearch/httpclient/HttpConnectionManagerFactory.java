package com.cnpc.geo.energyinformationsearch.httpclient;

/**
 * 
 * @Package:com.cnpc_dlp.linuxmonitor.client
 * @Description:http连接管理
 * @author:quwu
 * @Version：v1.0
 * @ChangeHistoryList：version     author         date              description 
 *                      v1.0        quwu    2014-9-17 下午8:27:09
 */
import java.io.FileInputStream;
import java.nio.charset.CodingErrorAction;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.apache.http.Consts;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.MessageConstraints;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

/**
 * Http链接的工厂类
 * 
 */
public class HttpConnectionManagerFactory {

	public static String keyPassword;
	public static String trustPassword;
	public static String keyStorePath;
	public static String trustStorePath;

	private static PoolingHttpClientConnectionManager connManager = null;
	static {
		try {
			keyPassword = ConfigInfo.getKeyPassword();
			trustPassword = ConfigInfo.getTrustPassword();
			keyStorePath = System.getProperty("user.dir") + ConfigInfo.getKeyStorePath();
			trustStorePath = System.getProperty("user.dir") + ConfigInfo.getTrustStorePath();
			init();
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @Description：返回Http连接管理器
	 * @return PoolingHttpClientConnectionManager
	 * @author:quwu
	 * @Date :2014-10-27 上午10:24:40
	 */
	public static PoolingHttpClientConnectionManager getClientConnectionManager() {
		return connManager;
	}

	/**
	 * 
	 * @Description：初始化Http连接管理器
	 * @throws Exception
	 *             void
	 * @author:quwu
	 * @Date :2014-10-27 上午10:25:03
	 */
	public static void init() throws Exception {
		// 实例化SSL上下文,双向认证
		/*
		 * SSLContext sslContext= getSSLContext(keyPassword, trustPassword,
		 * keyStorePath, trustStorePath);
		 * 
		 * SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
		 * sslContext, SSLConnectionSocketFactory.STRICT_HOSTNAME_VERIFIER);
		 */
		// 信任所有
		SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {

			public boolean isTrusted(X509Certificate[] chain, String authType) {
				return true;
			}
		}).build();

		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);

		// 注册http,https
		Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create()// .register("http",
																													// plainsf)
				.register("http", PlainConnectionSocketFactory.INSTANCE).register("https", sslsf).build();
		// 实例化http连接池
		connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
		// 配置socket参数
		// Create socket configuration
		SocketConfig socketConfig = SocketConfig.custom().setTcpNoDelay(true).build();
		connManager.setDefaultSocketConfig(socketConfig);
		// Create message constraints
		MessageConstraints messageConstraints = MessageConstraints.custom().setMaxHeaderCount(200).setMaxLineLength(2000).build();
		// Create connection configuration
		ConnectionConfig connectionConfig = ConnectionConfig.custom().setMalformedInputAction(CodingErrorAction.IGNORE).setUnmappableInputAction(CodingErrorAction.IGNORE).setCharset(Consts.UTF_8).setMessageConstraints(messageConstraints).build();
		// 设置连接配置
		connManager.setDefaultConnectionConfig(connectionConfig);
		// 设置最大连接数
		connManager.setMaxTotal(200);
		// 设置每个路由最大连接数
		connManager.setDefaultMaxPerRoute(20);

	}

	/**
	 * 
	 * @Description：实例化SSL上下文
	 * @param keeypass
	 * @param trustpass
	 * @param keyStorePath
	 * @param trustStorePath
	 * @return
	 * @throws Exception
	 *             SSLContext
	 * @author:quwu
	 * @Date :2014-10-27 上午10:25:37
	 */
	public static SSLContext getSSLContext(String keeypass, String trustpass, String keyStorePath, String trustStorePath) throws Exception {
		// 实例化密钥库
		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		// 获得密钥库
		KeyStore keyStore = getKeyStore(keyPassword, keyStorePath);
		// 初始化密钥工厂
		keyManagerFactory.init(keyStore, "123456".toCharArray());
		// keyManagerFactory.init(keyStore, this.keyPassword.toCharArray());

		// 实例化信任库
		TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		// 获得信任库
		KeyStore trustStore = getKeyStore(trustPassword, trustStorePath);
		// 初始化信任库
		trustManagerFactory.init(trustStore);
		// 实例化SSL上下文
		SSLContext ctx = SSLContext.getInstance("TLS");
		// 初始化SSL上下文
		ctx.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
		// 获得SSLSocketFactory
		return ctx;
	}

	/**
	 * 
	 * @Description：读取密钥库
	 * @param password
	 * @param keyStorePath
	 * @return
	 * @throws Exception
	 *             KeyStore
	 * @author:quwu
	 * @Date :2014-10-27 上午10:26:15
	 */
	public static KeyStore getKeyStore(String password, String keyStorePath) throws Exception {
		// 实例化密钥库
		KeyStore ks = KeyStore.getInstance("JKS");
		// 获得密钥库文件流
		FileInputStream is = new FileInputStream(keyStorePath);
		// 加载密钥库
		ks.load(is, password.toCharArray());
		// 关闭密钥库文件流
		is.close();
		return ks;
	}
}
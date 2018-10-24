package com.cnpc.geo.energyinformationsearch.thrift.server;

import org.apache.log4j.Logger;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TSSLTransportFactory.TSSLTransportParameters;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;

import com.cnpc.geo.energyinformationsearch.base.entity.CommonParameters;
import com.cnpc.geo.energyinformationsearch.base.init.BaseRunnable;
import com.cnpc.geo.energyinformationsearch.thrift.inface.SearchInfoService;

public class ThriftServiceServer extends BaseRunnable {

	/**
	 * 启动thrift服务器
	 * 
	 * @param args
	 */
	Logger logger = Logger.getLogger(ThriftServiceServer.class);
	private SearchInfoService.Iface searchInfo;

	private ThriftSSLConfig thriftSSLConfig;

	@Override
	public void execute() {
		try {
			TSSLTransportParameters params = new TSSLTransportParameters();
			params.setKeyStore(System.getProperty("EnergyInformationSearch.root") + thriftSSLConfig.getPath(), CommonParameters.KEYSTOREPASS);
			TServerSocket serverTranpoort = TSSLTransportFactory.getServerSocket(CommonParameters.PROT, CommonParameters.TIMEOUT, null, params);
			TProcessor processor = new SearchInfoService.Processor<SearchInfoService.Iface>(searchInfo);
			TThreadPoolServer.Args ttpsArgs = new TThreadPoolServer.Args(serverTranpoort);
			ttpsArgs.processor(processor);
			ttpsArgs.maxWorkerThreads(CommonParameters.MAXPOOLSIZE);
			ttpsArgs.minWorkerThreads(CommonParameters.MINPOOLSIZE);
			ttpsArgs.protocolFactory(new TBinaryProtocol.Factory());
			TServer server = new TThreadPoolServer(ttpsArgs);
			logger.info("Start server on port 9091....");
			server.serve();
		} catch (TTransportException e) {
			logger.error("ThriftServiceServer---->>" + e);
		}
	}

	public ThriftSSLConfig getThriftSSLConfig() {
		return thriftSSLConfig;
	}

	public void setThriftSSLConfig(ThriftSSLConfig thriftSSLConfig) {
		this.thriftSSLConfig = thriftSSLConfig;
	}

	public SearchInfoService.Iface getSearchInfo() {
		return searchInfo;
	}

	public void setSearchInfo(SearchInfoService.Iface searchInfo) {
		this.searchInfo = searchInfo;
	}
}
package com.cnpc.geo.energyinformationsearch.thrift.client;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TSSLTransportFactory.TSSLTransportParameters;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.springframework.beans.factory.annotation.Autowired;

import com.cnpc.geo.energyinformationsearch.thrift.inface.DReqData;
import com.cnpc.geo.energyinformationsearch.thrift.inface.DResData;
import com.cnpc.geo.energyinformationsearch.thrift.inface.SearchInfoService;

public class ThriftClientService {

	@Autowired
	HttpServletRequest request;

	public static void main(String[] args) throws IOException {
		try {
			// new ThriftClientService().test();
			TSSLTransportParameters params = new TSSLTransportParameters();
			// 配置https
			params.setTrustStore("D://ee//truststore.jks", "123456", "SunX509", "JKS");
			TTransport transport = null;
			// transport = new TSocket(CommonParameters.IPAddRESS,
			// CommonParameters.PROT, CommonParameters.TIMEOUT);
			transport = TSSLTransportFactory.getClientSocket("10.2.45.88", 9091, 10000, params);
			TProtocol protocol = new TBinaryProtocol(transport);
			SearchInfoService.Client client = new SearchInfoService.Client(protocol);
			if (transport.isOpen() == false) {
				transport.open();
			}
			DReqData request = new DReqData();
			// DResData data = client.download(request);
			// System.out.println(new
			// String(data.getOriginalFile())+"-------------");
			request.setSequenceID("1234");
			request.setHID("");
			DResData data1 = client.upload(request);
			System.out.println(new String(data1.getSwfFile()) + "--------------------");
		} catch (TTransportException e) {
			e.printStackTrace();
		} catch (TException e) {
			e.printStackTrace();
		}
	}
}
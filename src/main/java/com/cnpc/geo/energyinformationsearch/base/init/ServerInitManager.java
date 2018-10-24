package com.cnpc.geo.energyinformationsearch.base.init;

import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 
 * @Package: com.cnpc.geo.crawlmaster.base.controller
 * @ClassName: ServerInitManager
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author quwu
 * @date 2015年7月13日 下午5:44:35
 * @version V1.0
 * @ChangeHistoryList    version    modifier        date                description
 *                        V1.0        quwu     2015年7月13日 下午5:44:35
 */

public class ServerInitManager implements InitializingBean {
	
	
	private ThreadPoolTaskExecutor taskExecutor;
	
	
	private List<Object> serverList;
	
	
	public ThreadPoolTaskExecutor getTaskExecutor() {
		return taskExecutor;
	}


	public void setTaskExecutor(ThreadPoolTaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}


	public List<Object> getServerList() {
		return serverList;
	}


	public void setServerList(List<Object> serverList) {
		this.serverList = serverList;
	}


	public void initialize() {
		Iterator<Object> it = serverList.iterator();
		while (it.hasNext()) {
			taskExecutor.execute((Runnable) it.next());
		}
	}
	
	
	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		initialize();
	}
	
	

}

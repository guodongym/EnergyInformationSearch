package com.cnpc.geo.energyinformationsearch.job;
/**
 * 
 * @Package:com.cnpc_dlp.linuxmonitor.stateoftime
 * @Description:
 * @author:quwu
 * @Version：v1.0
 * @ChangeHistoryList：version     author         date              description 
 *                      v1.0        quwu    2014-9-17 下午8:18:23
 */
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SingleExecutorUtil {
	private static ExecutorService exec;
	private static Object lock=new Object();
	public static ExecutorService getExec(){
		synchronized(lock){
			if(exec==null){
				exec = Executors.newSingleThreadExecutor();
			}
		}
		return exec;
	}
	
	public static void execTask(Runnable task){
		ExecutorService executor = getExec();
		executor.execute(task);
	}

}

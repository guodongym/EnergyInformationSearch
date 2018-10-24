package com.cnpc.geo.energyinformationsearch.base.init;
/**
 * 
 * @author quwu
 *
 */
public abstract class BaseRunnable implements Runnable {
	
	
	@Override
	public void run() {
		try{
		InitLock.getNewInitLock().lock();
		// TODO Auto-generated method stub
		try {
			if(InitLock.getFlag().get()==false)
				InitLock.getNewInitCondition().await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};
		}finally{
			InitLock.getNewInitLock().unlock();
		}
		execute();
		
	}
	
	public abstract void execute();

}

package com.cnpc.geo.energyinformationsearch.base.init;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 
 * @author quwu
 *
 */
public class InitLock {
	
	
    public static ReentrantLock getNewInitLock() {
		return newInitLock;
	}

	public static void setNewInitLock(ReentrantLock newInitLock) {
		InitLock.newInitLock = newInitLock;
	}

	public static Condition getNewInitCondition() {
		return newInitCondition;
	}

	public static void setNewInitCondition(Condition newInitCondition) {
		InitLock.newInitCondition = newInitCondition;
	}

	public static AtomicBoolean getFlag() {
		return flag;
	}

	public static void setFlag(AtomicBoolean flag) {
		InitLock.flag = flag;
	}

	private static ReentrantLock newInitLock = new ReentrantLock();

    private static Condition newInitCondition = newInitLock.newCondition();
    
    private static AtomicBoolean  flag = new AtomicBoolean(false);


}

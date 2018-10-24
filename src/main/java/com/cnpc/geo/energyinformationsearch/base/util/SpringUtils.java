/**
 *@description
 *@auth hzg
 *@date 2014-8-21下午3:18:17
 */

package com.cnpc.geo.energyinformationsearch.base.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @Package:com.cnpc.geo.crawlmaster.base.util
 * @Description:
 * @author:HZG
 * @Version：v1.0
 * @ChangeHistoryList：version author date description v1.0 HZG 2014-8-21
 *                            下午3:18:17
 */
public class SpringUtils implements ApplicationContextAware {

	private static ApplicationContext applicationContext;

	/**
	 * @Description：
	 * @param arg0
	 * @throws BeansException
	 * 
	 * @author:HZG
	 * @Date :2014-8-21 下午3:21:48
	 */
	@SuppressWarnings("static-access")
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	public static <T> T getBean(String name, Class<T> clazz) throws BeansException {
		return applicationContext.getBean(name, clazz);
	}

	@SuppressWarnings("unchecked")
	public static <T> T getBean(String name) throws BeansException {
		return (T) applicationContext.getBean(name);
	}

	/**
	 * 判断字符串是否为空
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isNullString(String str) {
		if ("".equals(str) && str == null) {
			return true;
		} else {
			return false;
		}
	}
}
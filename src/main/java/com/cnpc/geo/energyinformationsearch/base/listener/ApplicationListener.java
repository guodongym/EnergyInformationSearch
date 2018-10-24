package com.cnpc.geo.energyinformationsearch.base.listener;

import javax.servlet.ServletContextEvent;

import org.apache.log4j.Logger;
import org.springframework.web.context.ContextLoaderListener;

import com.cnpc.geo.energyinformationsearch.base.init.InitLock;
import com.cnpc.geo.energyinformationsearch.web.controller.SearchIfaceController;
/**
 * 
 * @author quwu
 *
 */
public class ApplicationListener extends ContextLoaderListener {  
	Logger logger = Logger.getLogger(SearchIfaceController.class);
	  
    public void contextDestroyed(ServletContextEvent sce) {  
        // TODO Auto-generated method stub  
  
    }  
  
    public void contextInitialized(ServletContextEvent sce) {  
        // TODO Auto-generated method stub  
        String webAppRootKey = sce.getServletContext().getRealPath("/");  
        System.setProperty("EnergyInformationSearch.root" , webAppRootKey);  
        String path =System.getProperty("EnergyInformationSearch.root");  
        logger.info("EnergyInformationSearch.root:"+path);
        InitLock.getFlag().set(true);
        
        try{
        	InitLock.getNewInitLock().lock();
            InitLock.getNewInitCondition().signal();
        }finally{
        	InitLock.getNewInitLock().unlock();
        }
    }  
  
}

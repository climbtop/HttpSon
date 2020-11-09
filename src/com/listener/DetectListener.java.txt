package com.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

public class DetectListener implements ServletContextListener {

	AppEngine ap = null;
	
	public void contextInitialized(ServletContextEvent arg0) {
		log.info("contextInitialized()======================start");
		int i = 0;
		while(true){
			String coreClass = arg0.getServletContext()
					.getInitParameter("coreClass"+(i<=0?"":String.valueOf(i)));
			if(coreClass!=null && coreClass.length()>0){
				try {
					 ap = (AppEngine)Class.forName(coreClass).newInstance();
					 new Thread(ap).start();
				} catch (Exception e) {
					log.error("contextInitialized() Error. coreClass:"+coreClass, e);
				}
			}else{
				break;
			}
			i = i+1;
		}
		
	}

	public void contextDestroyed(ServletContextEvent arg0) {
		log.info("contextDestroyed()======================stop");
		if(ap!=null){
			ap.stop();
		}
	}

	private Logger log = Logger.getLogger(DetectListener.class);
}

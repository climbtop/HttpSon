package com.core;

import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.common.FileUtil;
import com.httpclient.HttpData;
import com.httpclient.HttpSession;
import com.listener.AppEngine;
import com.listener.DetectListener;


class HttpDataTest extends HttpData{
	private String val = "Unknown";
	public String getVal() {
		return val;
	}
	public HttpDataTest(String url){
		super(url);
	}
	public synchronized  void  callback(){
		String[] values = null;
		String regex = null;
		
		val = "Unknown";
		regex = "<hr>\\s*(.*)<hr>";
		values = this.parserContentByRegex(regex);
		if(values!=null && values.length>0){
			val = values[0];
		}
	}
}

public class CoreRun implements AppEngine{

	private  String rememberPad = "./nic/mem.txt";
	private  String namePad = "./nic/name.txt";
	private  String successPad = "./nic/success.txt";
	private  Integer savePerCount = 100;
	
	public static void main(String[] args)throws Exception{
		new CoreRun().action();
	}
	
	public void init(){
		try{
			Properties properties = new Properties();
			properties.load(CoreRun.class.getClassLoader().
					getResourceAsStream("conf.properties"));
			String padId =  properties.getProperty("pad.id");
			
			rememberPad =  properties.getProperty("pad.remember");
			rememberPad = rememberPad.replace("{id}", padId);
			
			namePad =  properties.getProperty("pad.name");
			namePad = namePad.replace("{id}", padId);
			
			successPad =  properties.getProperty("pad.success");
			successPad = successPad.replace("{id}", padId);
			
			savePerCount =  Integer.valueOf(properties.getProperty("pad.percount"));
		}catch(Exception e){
			log.info("init() Error", e);
		}
	}
	
	public void action(){
		init();
		log.info("rememberPad:"+rememberPad);
		log.info("namePad:"+namePad);
		log.info("successPad:"+successPad);
		log.info("savePerCount:"+savePerCount);
		String name = getName();
		int current = getRowNum();
		log.info("CoreRun action()........... name:"+name+",curNum:"+current);
		
		HttpSession httpSesssion = new HttpSession();
		HttpDataTest httpData1 = new HttpDataTest("https://www.onlinenic.com/cgi-bin/english/a_query_id.cgi");
		
		httpData1.setPostMethod("POST");
		httpData1.setPostData("email", "kinb@21cn.com");
		httpData1.setPostData("registrant_e", name);
		
		int len = 567095;
		int start = (current>1000?current:1000) ;
		int count = 0;
		
		log.info("start......................");
		for(int i= start; i<len; i++){
			
			httpData1.setPostData("customer_id", String.valueOf(i));
			httpSesssion.visitURL(httpData1);
			
			String value = httpData1.getVal();
			if(value!=null && value.indexOf("(110)")>=0){
				log.info("rowNum:"+i+" (110)");
			}else{
				log.info("rowNum:"+i+" "+value);
				FileUtil.writeLocalFile(successPad, String.valueOf(value));
			}
			
			count ++;
			if(count>=savePerCount){
				saveRowNum(i);
				count = 0;
			}
		}
	}
	
	
	public  void saveRowNum(int num){
		FileUtil.clearLocalFile(rememberPad);
		FileUtil.writeLocalFile(rememberPad, String.valueOf(num));
	}

	public  int getRowNum(){
		List<String>  list = FileUtil.readLocalFile(rememberPad);
		if(list.size()>0){
			String val = list.get(0);
			try{
				if(val!=null&&val.length()>0){
					return Integer.valueOf(val).intValue();
				}
			}catch(Exception e){
				return -1;
			}
		}
		return -1;
	}
	
	public  String getName(){
		String defaultName = "Jinbo Yao";
		List<String>  list = FileUtil.readLocalFile(namePad);
		if(list.size()>0){
			String val = list.get(0);
			try{
				if(val!=null&&val.length()>0){
					return val.trim();
				}
			}catch(Exception e){
				return defaultName;
			}
		}
		return defaultName;
	}
	
	
	public void stop() {

	}
	public void run() {
		try {
			action();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private static Logger log = Logger.getLogger(DetectListener.class);
}

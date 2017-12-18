package com.common.extend.web;

import java.io.File;

import com.extend.HttpGet;
import com.httpclient.HttpHelper;
import com.httpclient.ValidDialog;

public class PromptDialog {

	private String url = null;
	private String tmp = null;
	private HttpHelper hs = null;
	public static String path = null;
	public static String ready = null;
	
	public PromptDialog(HttpHelper hs, String url){
		this(hs,url,System.getProperty("java.io.tmpdir").replaceAll("\\\\", "/"));
	}
	
	public PromptDialog(HttpHelper hs, String url, String tmp){
		this.hs = hs;
		this.url = url;
		this.tmp = tmp;
	}

	private String visit(){
		if(!new File(tmp).exists()) new File(tmp).mkdirs();
		File[] files = new File(tmp).listFiles();
		for(File f : files) {
			if(f.getName().matches("^\\d+\\.jpg$")){
				f.delete();
			}
		}
		try{
			HttpGet get = new HttpGet(url);
			get.setTimeout(50*1000);
			get.setUsedPool(true);
			hs.visitURI(get);
			return get.saveBinary(tmp, ".jpg");
		}catch(Throwable e){
		}
		return "";
	}
	
	public String getConsoleCode(){
		path = visit();
		ValidDialog vd = new ValidDialog(path);
		String code = vd.getValidcode();
		return code;
	}
	
	public String getWebCode(){
		path = visit();
		ready = null;
		while(ready==null){
			try{
				Thread.sleep(2000);
			}catch(Exception e){
				break;
			}
			if(ready!=null) return ready; 
		}
		return "";
	}
	
}

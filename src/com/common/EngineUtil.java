package com.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class EngineUtil {

	public static Object getEngineInstance(String files, String enc, String codes){
		try{
			Object sem = Class.forName("javax.script.ScriptEngineManager").newInstance();
			Object engine = ClassUtil.invoke(sem, "getEngineByName", "JavaScript");
			if(engine ==null ){
				engine = Class.forName("com.sun.script.javascript.RhinoScriptEngine").newInstance();
			}
			
			if(files!=null && files.length()>0 ){
				String[] jspaths = files.split(",");
				for(int i=0; i<jspaths.length; i++){
				      String jspath = jspaths[i];
					  jspath = jspath.trim();
					  if(jspath.length()<=0)continue;
					  try{
						  String src = jspath;
						  Reader reader = (Reader)(new InputStreamReader(new FileInputStream(new File(src)),enc));
						  ClassUtil.invoke(engine,"eval",reader);
					  }catch(Exception e){
						  e.printStackTrace();
					  }
				}
			}
			
			if(codes!=null && codes.length()>0 ){
				  try{
					  ClassUtil.invoke(engine,"eval",codes);
				  }catch(Exception e){
					  e.printStackTrace();
				  }
			}
			  
			return engine;
		}catch(Throwable e){
			e.printStackTrace();
		}
		return null;
	}
	
	public static Object getEngineInstance(String codes){
		return getEngineInstance(null,null,codes);
	}
	
	public static Object getEngineInstance(String files, String enc){
		return getEngineInstance(files,enc,null);
	}
}

package com.common.extend.util;

import java.io.File;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import com.common.FileUtil;

public class CommResMgr {

	public String context = "/";
	public String encoding = "utf-8";
	
	public void loadQueue(String key, Queue queue, Set set){
		List list = FileUtil.readFile(getPath(key),encoding);
		for(int i=0; i<list.size(); i++){
			Object val = list.get(i);
			if(!set.contains(val)){
				queue.offer(val);
			}
		}
	}
	
	public void loadSet(String key, Set set){
		List list = FileUtil.readFile(getPath(key+"_set"),encoding);
		for(int i=0; i<list.size(); i++) set.add(list.get(i));
	}
	
	public String getPath(String realPath, String name){
		realPath = realPath==null?"/":realPath;
		if(!new File(realPath).exists()){
			new File(realPath).mkdirs();
		}
		if(!realPath.endsWith("/"))
			realPath += "/";
		return realPath+name+".txt";
	}
	public String getPath(String name){
		return getPath(context, name);
	}
	
	public void insert(String key, Object val, Queue queue, Set set){
		if( !queue.contains(val) && !set.contains(val) ){
			queue.offer(val);
			FileUtil.writeFile(getPath(key), String.valueOf(val),encoding);
		}
	}	
	
	public Object next(Queue queue){
		return queue.poll();
	}
	
	public Object peek(Queue queue){
		return queue.peek();
	}
	
	public void complete(String key, Object val, Set set){
		set.add(val);
		FileUtil.writeFile(getPath(key+"_set"), String.valueOf(val),encoding);
	}
	
}

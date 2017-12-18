package com.common.extend.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ListResMgr extends CommResMgr {

	private ConcurrentLinkedQueue list = new ConcurrentLinkedQueue();
	private Set listSet = Collections.synchronizedSet(new HashSet());
	private String key = null;
	private int maxSize = -1;
	
	public ListResMgr(String key, String context){
		this(key,context,"utf-8");
	}
	
	public ListResMgr(String key, String context, String encoding){
		this.context = context;
		this.key = key;
		this.encoding = encoding;
		this.loadSet(key, listSet);
		this.loadQueue(key, list, listSet);
	}
	
	public void setMaxSize(int maxSize){
		this.maxSize = maxSize;
	}
	
	public void add(String val){
		this.insert(key, val, list, listSet);
	}
	
	public void fix(String val){
		this.complete(key, val, listSet);
	}
	
	public String next(){
		return (String)this.next(list);
	}
	
	public String peek(){
		return (String)this.peek(list);
	}
	
	public int sizeUser(){
		return list.size();
	}
	
	public int total(){
		return listSet.size();
	}
	
	public int max(){
		return this.maxSize;
	}
}

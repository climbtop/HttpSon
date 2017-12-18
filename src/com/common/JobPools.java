package com.common;

import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

/**
 *  线程池简化版处理
 */
public class JobPools extends Thread{
	
	private int maxThreadSize = 40; //最大线程数
	private int maxBlockSize  = 600;//最大列表
	private boolean isRunning = false; //是否运行标记
	private boolean stopFlag = false; //是否结束标记
	private LinkedList<Runnable> cacheList = null;	//处理缓冲列表.
	private ThreadPoolExecutor threadPool = null;
	
	public JobPools(){
		init();
	}
	
	public JobPools(int maxThreadSize, int maxBlockSize){
		this.maxBlockSize = maxBlockSize;
		this.maxThreadSize = maxThreadSize;
		init();
	}

	public void init(){
		cacheList = new LinkedList<Runnable>();
		threadPool = 
			new ThreadPoolExecutor((maxThreadSize/2+1), maxThreadSize, 5,
			TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(maxBlockSize),
			new ThreadPoolExecutor.AbortPolicy()
		);
	}
	
	//JobPool开始执行
	public void run(){
		if(isRunning())return;
		setRunning(true);
		while(true){
			if(isStopFlag())break;
			LinkedList<Runnable> tempList = runCacheList();
			while(tempList!=null && tempList.size()>0){
				try{
					execute(tempList);
					Thread.sleep(30);
				}catch(Exception e){
					log.debug("JobPools run() throws Excpeion", e);
				}
				tempList = runCacheList();
			}
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
			} 
		}
		setRunning(false);
	}
	
	//处理一批数据
	private void execute(LinkedList<Runnable> list){
		// 构造一个线程池
		for(Runnable r : list){
			try {
				while(true){
					try{
						threadPool.execute(r);
						break;
					}catch(RejectedExecutionException ree){
					}
					Thread.sleep(100); 
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 添加一任务
	 * @param job
	 */
	public void addJob(Runnable job){
		if(job==null) return;
		synchronized(cacheList){
			cacheList.addLast(job);
		}
		if(!isRunning()){
			new Thread(this).start();
		}
	}
	
	public LinkedList<Runnable> runCacheList() {
		synchronized(cacheList){
			LinkedList<Runnable> tempList = cacheList;
			cacheList = new LinkedList<Runnable>();
			return tempList;
		}
	}
	
	public LinkedList<Runnable> getCacheList() {
			return cacheList;
	}
	
	public void setCacheList(LinkedList<Runnable> cacheList) {
		this.cacheList = cacheList;
	}
	
	public boolean isRunning() {
		return isRunning;
	}

	private void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}
	
	public boolean isStopFlag() {
		return stopFlag;
	}

	public void setStopFlag(boolean stopFlag) {
		this.stopFlag = stopFlag;
	}
	
	/**
	 * 调试信息对象
	 */
	private Logger log = Logger.getLogger(getClass());
}

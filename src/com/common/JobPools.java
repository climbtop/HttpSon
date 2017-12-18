package com.common;

import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

/**
 *  �̳߳ؼ򻯰洦��
 */
public class JobPools extends Thread{
	
	private int maxThreadSize = 40; //����߳���
	private int maxBlockSize  = 600;//����б�
	private boolean isRunning = false; //�Ƿ����б��
	private boolean stopFlag = false; //�Ƿ�������
	private LinkedList<Runnable> cacheList = null;	//�������б�.
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
	
	//JobPool��ʼִ��
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
	
	//����һ������
	private void execute(LinkedList<Runnable> list){
		// ����һ���̳߳�
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
	 * ���һ����
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
	 * ������Ϣ����
	 */
	private Logger log = Logger.getLogger(getClass());
}

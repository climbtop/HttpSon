package com.util.concurrent;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * 重新改写为JDK1.6的线程池类,增加对线程池正在运行的监控
 */
public class ThreadPoolExecutant extends ThreadPoolExecutor{

	public ThreadPoolExecutant(int corePoolSize, int maximumPoolSize,
			long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
	}

	public ThreadPoolExecutant(int corePoolSize, int maximumPoolSize,
			long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory,
			RejectedExecutionHandler handler) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
				threadFactory, handler);
	}

	public ThreadPoolExecutant(int corePoolSize, int maximumPoolSize,
			long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
				threadFactory);
	}

	public ThreadPoolExecutant(int corePoolSize, int maximumPoolSize,
			long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
	}
	
	protected ConcurrentMap threadMap = new ConcurrentHashMap();
	protected ConcurrentMap startTimeMap = new ConcurrentHashMap();
	
	protected void beforeExecute(Thread t, Runnable r) {
		threadMap.put(r, t);
		startTimeMap.put(r, System.currentTimeMillis());
	}

	protected void afterExecute(Runnable r, Throwable t) {
		threadMap.remove(r);
		startTimeMap.remove(r);
	}
	
	public void shutdownThread(Thread t){
		super.workerDone(t);
	}
	
	public List getActiveTask(){
		LinkedList list = new LinkedList();
		Iterator it = threadMap.keySet().iterator();
		for(; it.hasNext(); ){
			Runnable r = (Runnable)it.next();
			list.addLast(r);
		}
		return list;
	}
	
	public Thread getCurrentThread(Runnable r){
		return (Thread)threadMap.get(r);
	}
	
	public Long getStartTime(Runnable r){
		return (Long)startTimeMap.get(r);
	}
	
	public String getActiveTaskName(){
		return getActiveTaskName(null);
	}
	
	public String getActiveTaskName(CallBack cb){
		StringBuffer sb = new StringBuffer();
		List list = getActiveTask();
		for(Object o : list){
			if(!(o instanceof Runnable))
				continue;
			Runnable r = (Runnable)o;
			if(cb != null){
				sb.append(cb.handle(r));
			}else{
				sb.append(r.getClass().getCanonicalName()+"\r\n");
			}
		}
		return sb.toString();	
	}
	
}

package com.common;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Vector;

public class ConcurrentKit {

	protected int threadCount;
	protected long joinWait;
	protected String taskName;
	protected OutputStream outputStream;
	protected int minCount=-1;
	protected int minWait=-1;
	
	public ConcurrentKit() {
		this("",1);
	}

	public ConcurrentKit(String taskName) {
		this(taskName,1);
	}
	
	public ConcurrentKit(String taskName, int threadCount) {
		this.taskName = taskName;
		this.threadCount = threadCount;
	}
	
	public void setJoinWaker(int minCount, int minWait){
		this.minCount = minCount;
		this.minWait = minWait;
	}
	
	protected boolean hasWriter(){
		return outputStream!=null;
	}
	
	protected PrintWriter getWriter(){
		return new PrintWriter(outputStream);
	}

	public <K, V> void parallel(List<K> list, ListTask<K> runTask) {
		Map<K, V> targetData = Collections.synchronizedMap(new HashMap<K,V>());
		for (K key : list) {
			targetData.put(key, null);
		}
		parallelRun(targetData, runTask);
	}
	
	public <K, V> void parallel(Queue<K> queue, QueueTask<K> runTask) {
		Map<K, V> targetData = Collections.synchronizedMap(new HashMap<K,V>());
		for (K key : queue) {
			targetData.put(key, null);
		}
		parallelRun(targetData, runTask);
	}

	public <K, V> void parallel(Map<K, V> map, MapTask<K, V> runTask) {
		Map<K, V> targetData = Collections.synchronizedMap(new HashMap<K,V>());
		targetData.putAll(map);
		parallelRun(targetData, runTask);
	}
	
	protected <K, V> void parallelRun(final Map<K, V> targetData, final Object runTask) {
		Thread[] threadGroup = new Thread[threadCount];
		Thread wakerThread = null;
		if (minCount >= 0 && minWait >= 0) {
			wakerThread = new Thread(new JoinWaker(threadGroup, minCount, minWait));
		}
		final Set<K> doneData = Collections.synchronizedSet(new HashSet<K>());

		for (int i = 0; i < threadGroup.length; i++) {
			java.lang.Runnable r = new java.lang.Runnable() {
				@SuppressWarnings("unchecked")
				public void run() {
					if(hasWriter()){
						getWriter().println(Thread.currentThread().getName() + " started...");
					}
					
					List<K> list = new Vector<K>(targetData.keySet());
					Collections.shuffle(list);
					
					for (K key : list) {
						if (doneData.contains(key)) {
							continue;
						}
						doneData.add(key);
						
						try {
							if(runTask instanceof ListTask){
								((ListTask<K>)runTask).run(key);
							}
							if(runTask instanceof QueueTask){
								((QueueTask<K>)runTask).run(key);
							}
							if(runTask instanceof MapTask){
								V value = targetData.get(key);
								((MapTask<K,V>)runTask).run(key, value);
							}
						} catch (Exception e) {
							if(hasWriter()){
								getWriter().println(taskName + " process for " + String.valueOf(key));
								e.printStackTrace(getWriter());
							}
						}

						Thread.yield();
						if(hasWriter()){
							getWriter().println(taskName + " progress (" + (doneData.size() + "/" + targetData.size())+")");
						}
					}
					if(hasWriter()){
						getWriter().println(Thread.currentThread().getName() + " ended.");
					}
				}
			};
			threadGroup[i] = new Thread(r, "Thread-" + (i + 1));
		}

		for (Thread thread : threadGroup) {
			thread.start();
		}
		
		if(wakerThread!=null) wakerThread.start();

		for (Thread thread : threadGroup) {
			try {
				thread.join(joinWait);
			} catch (InterruptedException e) {
				if(hasWriter()){
					e.printStackTrace(getWriter());
				}
			}
		}
		
		for (Thread thread : threadGroup) {
			thread.interrupt();
		}
		
		if(wakerThread!=null) wakerThread.interrupt();
	}

	public OutputStream getOutputStream() {
		return outputStream;
	}

	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	public int getThreadCount() {
		return threadCount;
	}

	public void setThreadCount(int threadCount) {
		this.threadCount = threadCount;
	}

	public long getJoinWait() {
		return joinWait;
	}

	public void setJoinWait(long joinWait) {
		this.joinWait = joinWait;
	}

	public interface ListTask<K> {
		public void run(K key);
	}
	
	public interface QueueTask<K> {
		public void run(K key);
	}
	
	public interface MapTask<K, V> {
		public void run(K key, V value);
	}

}

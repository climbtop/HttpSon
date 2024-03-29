package com.common;

import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPoolUtil {
	public static int CORE_POOL_SIZE = 0;
	private static volatile ThreadPoolUtil mInstance;
	
	private int corePoolSize;
	private int maxPoolSize;
	private long keepAliveTime = 20;
	private TimeUnit unit = TimeUnit.MINUTES;

	private ThreadPoolExecutor executor;

	private ThreadPoolUtil() {
		corePoolSize = getCpuCount() * 2 + 1;
		maxPoolSize = corePoolSize;
		if (CORE_POOL_SIZE >= 12) {
			maxPoolSize = CORE_POOL_SIZE;
			corePoolSize = CORE_POOL_SIZE;
		}
		executor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, unit,
				new LinkedBlockingQueue<Runnable>(), new DefaultThreadFactory(Thread.NORM_PRIORITY, "thread-pool-"),
				new ThreadPoolExecutor.AbortPolicy());
	}

	public static ThreadPoolUtil getInstance() {
		if (mInstance == null) {
			synchronized (ThreadPoolUtil.class) {
				if (mInstance == null) {
					mInstance = new ThreadPoolUtil();
				}
			}
		}
		return mInstance;
	}

	public void execute(Runnable runnable) {
		if (runnable != null) {
			executor.execute(runnable);
		}
	}

	public Future<?> submit(Runnable runnable) {
		if (runnable != null) {
			return executor.submit(runnable);
		}
		return null;
	}

	public boolean remove(Runnable runnable) {
		if (runnable != null) {
			return executor.remove(runnable);
		}
		return false;
	}

	private static class DefaultThreadFactory implements ThreadFactory {
		private static final AtomicInteger poolNumber = new AtomicInteger(1);
		private final AtomicInteger threadNumber = new AtomicInteger(1);
		private final ThreadGroup group;
		private final String namePrefix;
		private final int threadPriority;

		DefaultThreadFactory(int threadPriority, String threadNamePrefix) {
			this.threadPriority = threadPriority;
			this.group = Thread.currentThread().getThreadGroup();
			this.namePrefix = threadNamePrefix + poolNumber.getAndIncrement() + "-thread-";
		}

		@Override
		public Thread newThread(Runnable r) {
			Thread thread = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
			if (thread.isDaemon()) {
				thread.setDaemon(false);
			}
			thread.setPriority(threadPriority);
			return thread;
		}
	}

	public ThreadPoolExecutor getExecutor() {
		return executor;
	}
	
	public void shutdown() {
		if (executor != null) {
			executor.shutdown();
		}
	}
	
	public int getCpuCount() {
		return Runtime.getRuntime().availableProcessors();
	}
}

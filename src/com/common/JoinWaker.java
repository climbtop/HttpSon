package com.common;

public class JoinWaker implements Runnable{

	private Thread[] threadGroup;
	private int minCount;
	private int minWait;
	private boolean running;
	
	public JoinWaker(Thread[] threadGroup, int minCount, int minWait){
		this.threadGroup = threadGroup;
		this.minCount = minCount;
		this.minWait = minWait;
		this.running = true;
		if(minCount<=0) minCount = 0;
		if(minCount>threadGroup.length) {
			minCount = threadGroup.length;
		}
	}
	
	public void run() {
		long start = -1, end = -1;
		while(running){
			try{
				int alive = aliveCount();
				if(alive==0){
					break;
				}
				if(alive<=minCount){
					if(start<0){
						start = System.currentTimeMillis();
					}
				}else{
					start = -1;
				}
				if(start>0){
					end = System.currentTimeMillis();
					if(end - start >= minWait){
						killAll();
						break;
					}
				}
				Thread.sleep(20);
				Thread.yield();
			}catch(Exception e){
				killAll();
				break;
			}
		}
		killAll();
	}
	
	public boolean isRunning() {
		return running;
	}

	public int aliveCount(){
		int alive = 0;
		for(int i=0; i<threadGroup.length; i++){
			Thread t = threadGroup[i];
			if(t.isAlive()){
				alive ++;
			}
		}
		return alive;
	}
	
	public void killAll(){
		for(int i=0; i<threadGroup.length; i++){
			try{
				Thread t = threadGroup[i];
				if(t.isAlive()){
					t.interrupt();
				}
			}catch(Exception e){
			}
		}
	}
}

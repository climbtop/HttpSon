package com.common;

public class Timing {
	private long counter = 0;

	public Timing() {
		this.start();
	}

	public void start() {
		this.counter = System.currentTimeMillis();
	}

	public long count() {
		return System.currentTimeMillis() - this.counter;
	}

	public long reset() {
		long current = System.currentTimeMillis();
		long period = (current - this.counter);
		this.counter = current;
		return period;
	}
}

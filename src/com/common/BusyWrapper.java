package com.common;

public class BusyWrapper implements AutoCloseable {
	private String oldTag;
	private String newTag;

	public BusyWrapper(String newTag) {
		this.oldTag = Thread.currentThread().getName();
		this.newTag = newTag;
		Thread.currentThread().setName(getNewTag());
	}

	@Override
	public void close() {
		Thread.currentThread().setName(getOldTag());
	}

	public String getOldTag() {
		return oldTag;
	}

	public String getNewTag() {
		return newTag;
	}

}

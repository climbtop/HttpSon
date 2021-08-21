package com.common;

import java.io.File;

/**
 * 删除文件线程
 */
public 	class DeleteFile extends Thread {
	File file = null;

	public DeleteFile(String path) {
		file = new File(path);
	}

	public void run() {
		if (!file.exists())
			return;
		int times = 0;
		while (file.exists() && times < 30) {
			file.delete();
			if (file.exists()) {
				try {
					sleep(300);
					System.gc();
					times++;
				} catch (Exception e) {
				}
			}
		}
	}
}

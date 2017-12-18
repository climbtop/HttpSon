/**
 * 文   件：	DeleteFile.java
 * 创建日期：	2009-06-16
 * 文件说明：	
 * 创 建 者：朱孙盛
 * 重大修改记录：
 * 			2009-06-16 by 朱孙盛 ： 创建
 * 
 * Copyright (C) 2008 - 版权所有   2008 21CN Corp. Ltd
 */
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

/**
 * ��   ����	DeleteFile.java
 * �������ڣ�	2009-06-16
 * �ļ�˵����	
 * �� �� �ߣ�����ʢ
 * �ش��޸ļ�¼��
 * 			2009-06-16 by ����ʢ �� ����
 * 
 * Copyright (C) 2008 - ��Ȩ����   2008 21CN Corp. Ltd
 */
package com.common;

import java.io.File;

/**
 * ɾ���ļ��߳�
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

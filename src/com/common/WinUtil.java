package com.common;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class WinUtil {
	
	/**
	 * ��ʱ����һ����
	 * @param exe //����ľ���·��.
	 * @param name//������ֺ�Ľ�������.
	 * @return
	 */
	public static boolean runAt(String exe,String name, boolean opened){
		if(exe==null) return false;
		if(name==null || name.length()<0){
			name = exe.substring(exe.lastIndexOf("/")+1);
			name = exe.substring(exe.lastIndexOf("\\")+1);
		}
		try{
			int i = 0,max = 10;
			while(true){
				int seconds = new Date().getSeconds();
				if(seconds>55) seconds = 0;
				String time = WinUtil.nextMinute();
				Runtime.getRuntime().exec("at /delete /yes");// at /delete /yes
				// at 21:04 /interactive  "c:\vidalia.bat"
				String cmd = "at "+time+" "+(opened?"/interactive":"")+" \""+exe+"\" ";
				System.out.println(cmd);
				Process proc = Runtime.getRuntime().exec(cmd);
				proc.waitFor();

				Thread.sleep((60-seconds)*1000);
				if(WinUtil.findProc(name)||i>=max)break;
				i++;
			}
			return i<max;
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}
	public static boolean runAt(String exe,String name){
		return runAt(exe,name,false);
	}
	
	/**
	 * ��һ��ʱ�����
	 */
	public static String nextMinute(){
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		c.add(Calendar.MINUTE, 1);
		c.add(Calendar.SECOND, 3);
		String time = sdf.format(c.getTime());
		return time;
	}
	
	/**
	 * �ж������Ƿ����
	 */
	public static boolean findProc(String name){
		//tasklist /FI "IMAGENAME eq vidalia.exe"
		byte[] result = SysUtil.exec("tasklist /FI \"IMAGENAME eq "+name+"\"");
		try {
			String values = new String(result,"GBK").toLowerCase();
			return values.indexOf(name.toLowerCase())>=0;
		} catch (UnsupportedEncodingException e) {
		}
		return false;
	}
	
	/**
	 * ɾ��ĳ������
	 */
	public static void killProc(String name, boolean child){
		//taskkill /IM "vidalia.exe" /F /T
		try {
			Runtime.getRuntime().exec("taskkill /IM \""+name+"\" /F "+(child?"/T":""));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void killProc(String name){
		killProc(name,true);
	}
}

package com.common;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串处理工具类
 */
public class StringUtil {
	
	/**
	 * 往后搜索
	 */
	public static int indexOf(String cont, String[] arr,int start){
		int dex = cont.length();
		for(String val : arr){
			int x  = cont.indexOf(val,start);
			if(x>=start){
				dex = (x<dex?x:dex);
			}
		}
		return dex;
	}
	public static int indexOf(String cont, String[] arr){
		return indexOf(cont, arr,0);
	}
	
	/**
	 * 往前搜索
	 */
	public static int lastIndexOf(String cont, String[] arr){
		int dex = 0;
		for(String val : arr){
			int x  = cont.lastIndexOf(val)+val.length();
			if(x>=0){
				dex = (x>dex?x:dex);
			}
		}
		return dex;
	}
	
	/**
	 * 分析Content的内容,获取有用信息
	 */
	public static String getValue(String content,String regex){
		String[] values = getGroup(content,regex);
		return values!=null && values.length>0 ? values[0] : null;
	}
	
	/**
	 * 分析Content的内容,获取有用信息
	 */
	public static String[] getGroup(String content,String regex){
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(content);
		String[] result = new String[0];
		if(matcher.find()){
			result = new String[matcher.groupCount()];
			for(int i=1; i<=matcher.groupCount(); i++){
				String value = matcher.group(i);
				value = (value == null?"":value.trim());
				result[i-1] = value;
			}
		}
		return result;
	}
	
	/**
	 * 分析Content的内容列表,获取有用信息
	 */
	public static List<String[]> getGroups(String content,String regex){
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(content);
		List<String[]> resultList = new ArrayList<String[]>();
		while(matcher.find()){
			int groupCount = matcher.groupCount();
			if(groupCount>0){
				String[] result = new String[groupCount];
				for(int i=1; i<=groupCount; i++){
					String value = matcher.group(i);
					value = (value == null?"":value.trim());
					result[i-1] = value;
				}
				resultList.add(result);
			}
		}
		return resultList;
	}
	
	/**
	 * 分析Content的内容列表第一个,获取有用信息
	 */
	public static String[] getFirstGroup(String content,String regex){
		List<String[]> list = getGroups(content,regex);
		String[] result = new String[list.size()];
		for(int i=0; i<result.length; i++){
			String[] cols = list.get(i);
			result[i] = cols.length>0?cols[0]:"";
		}
		return result;
	}
	
	/**
	 * 多个换行,Tab等填入单个空白
	 */
	public static String blank(String content){
		return content.replaceAll("\\s+", " ");
	}
	
	/**
	 * 连接字符串
	 */
	public static String join(Object[] arr, Object split){
		if(arr == null ) return "";
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<arr.length; i++){
			sb.append(sb.length()>0?split:"");
			sb.append(arr[i]);
		}
		return sb.toString();
	}
	
	/**
	 * 连接字符串
	 */
	public static String join(Collection c, Object split){
		if(c == null ) return "";
		return join(c.toArray(),split);
	}
	
	public static boolean isEmpty(String s) {
		return s==null ||s.trim().length()==0;
	}
	
	public static boolean isNotEmpty(String s) {
		return !isEmpty(s);
	}
	
	/**
	 * 获取固定长度数字字符串
	 */
	public static String getFixedLength(int num, int len) {
		String result = String.valueOf(num);
		while (result.length() < len) {
			result = ("0" + result);
		}
		return result;
	}
	
	/**
	 * 异常转化为字符串
	 */
	public static final String getStackTrace(Throwable e, String cs) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString();
	}
}

package com.extend;

import java.util.List;

import com.common.StringUtil;

/**
 * Http使用时工具类
 */
public class Httper {
	/**
	 * 剥离内容信息的标签
	 * @param content
	 * @return
	 */
	public static String peelTags(String content){
		String temp = content;
		do{
			content = temp;
			temp = content.replaceAll(
					"<(.*?)\\s*[^>]*>([\\s\\S]*?)</\\1>", "$2");
		}while(!content.equals(temp));
		content = content.replaceAll("\\s+", " ");
		content = content.replaceAll("&nbsp;", " ");
		return content;
	}
	
	/**
	 * 剥离表格的内容信息(前提表格要正确的)
	 * @param columns
	 * @param content
	 * @return
	 */
	public static List getTabVals(int columns,String content){
        String regex = "(?i)<tr[^>]*>" ;
        for(int i=0; i<columns; i++){
        	regex = regex + "\\s*<td[^>]*>([\\s\\S]*?)</td>";
        }
        regex = regex + "\\s*</tr>(?-i)";
        
        List list = StringUtil.getGroups(content, regex);
        for(int j=0; j<list.size(); j++){
        	String[] arr = (String[])list.get(j);
        	for(int i=0; i<arr.length; i++){
        		arr[i] = peelTags(arr[i]);
        	}
        	list.set(j, arr);
        }
        return list;
	}

	/**
	 * 剥离表格的内容信息(自动计算列宽)
	 * @param content
	 * @return
	 */
	public static List getTabVals(String content){
		String row = substring("<tr","</tr>",true,content);
		String[] col = StringUtil.getFirstGroup(row,"(?i)<td.*?>([\\s\\S]*?)</td>(?i)");
		if(col==null || col.length==0){
			col = StringUtil.getFirstGroup(row,"(?i)<th.*?>([\\s\\S]*?)</th>(?i)");
		}
		return getTabVals(col.length,content);
	}
	
	/**
	 *根据固定标识取其值
	 */
	private static String getValByMarker(String marker, String content,String markerName){
		String regex = "(?i)value=(['\"])(.*?)\\1.*?"+markerName+"=(['\"])(?-i)"+marker+"\\3";
		String[] vals = StringUtil.getGroup(content, regex);
		if(vals !=null && vals.length>1 && vals[1].length()>0 ) return vals[1];
		regex = "(?i)"+markerName+"=(?-i)(['\"])"+marker+"\\1.*?(?i)value=(?-i)(['\"])(.*?)\\2";
		vals = StringUtil.getGroup(content, regex);
		if(vals !=null && vals.length>2 && vals[2].length()>0 ) return vals[2];
		return "";
	}
	public static String getValById(String marker, String content){
		return getValByMarker(marker, content,"id");
	}
	public static String getValByName(String marker, String content){
		return getValByMarker(marker, content,"name");
	}
	
	/**
	 * 截取出前后标识的字符串
	 * @param startword
	 * @param endword
	 * @param include
	 * @param content
	 * @return
	 */
	public static String subString(String startword, String endword, boolean include, String content){
		int start =  content.indexOf(startword);
		int end   =  content.indexOf(endword, start);
		if(start<0 || end<0 || start>end || content==null ) return "";
		return content.substring(
				start + (include?0:startword.length()), 
				end   + (include?endword.length():0) );
	}
	public static String subString(String startword, String endword, String content){
		return substring(startword,endword,false,content);
	}

	/**
	 * 截取出前后标识的字符串 不区分大小写
	 * @param startword
	 * @param endword
	 * @param include
	 * @param content
	 * @return
	 */
	public static String substring(String startword, String endword, boolean include, String content){
		String _startword = startword.toLowerCase();
		String _endword   = endword.toLowerCase();
		String _content   = content.toLowerCase();
		int start =  _content.indexOf(_startword);
		int end   =  _content.indexOf(_endword, start);
		if(start<0 || end<0 || start>end || content==null ) return "";
		return content.substring(
				start + (include?0:startword.length()), 
				end   + (include?endword.length():0) );
	}
	public static String substring(String startword, String endword, String content){
		return substring(startword,endword,false,content);
	}
	
	/**
	 * 删除掉前后标识的字符串(包括前面)
	 * @param startword
	 * @param endword
	 * @param content
	 * @return
	 */
	public static String delString(String startword,String endword, boolean preclude, String content){
		int start =  content.indexOf(startword);
		int end   =  content.indexOf(endword, start);
		if(start<0 || end<0 || start>end || content==null ) return content;
		String startStr = preclude?"":content.substring(0, start);
		return startStr + content.substring(end+endword.length());
	}
	public static String delString(String startword,String endword, String content){
		return delString(startword,endword, true , content);
	}
	
	/**
	 * 删除掉前后标识的字符串(包括前面) 不区分大小写
	 * @param startword
	 * @param endword
	 * @param content
	 * @return
	 */
	public static String delstring(String startword,String endword, boolean preclude, String content){
		String _startword = startword.toLowerCase();
		String _endword   = endword.toLowerCase();
		String _content   = content.toLowerCase();
		int start =  _content.indexOf(_startword);
		int end   =  _content.indexOf(_endword, start);
		if(start<0 || end<0 || start>end || content==null ) return content;
		String startStr = preclude?"":content.substring(0, start);
		return startStr + content.substring(end+endword.length());
	}
	public static String delstring(String startword,String endword, String content){
		return delString(startword,endword, true , content);
	}
	
	/**
	 * 填充空白
	 * @param content
	 * @return
	 */
	public static String blank(String content){
		return content.replaceAll("\\s+", " ").trim();
	}
	
	/**
	 * 字符串索引
	 * @param content
	 * @param words
	 * @return
	 */
	public static int indexof(String content, String words){
		String _words   = words.toLowerCase();
		String _content = content.toLowerCase();
		return _content.indexOf(_words);
	}
	
	/**
	 * 字符串索引
	 * @param content
	 * @param words
	 * @return
	 */
	public static int indexof(String content, String words, int fromIndex){
		String _words   = words.toLowerCase();
		String _content = content.toLowerCase();
		return _content.indexOf(_words, fromIndex);
	}
	
	/**
	 * 字符串索引
	 * @param content
	 * @param words
	 * @return
	 */
	public static int lastIndexof(String content, String words){
		String _words   = words.toLowerCase();
		String _content = content.toLowerCase();
		return _content.lastIndexOf(_words);
	}
	
	/**
	 * 字符串索引
	 * @param content
	 * @param words
	 * @return
	 */
	public static int lastIndexof(String content, String words, int fromIndex){
		String _words   = words.toLowerCase();
		String _content = content.toLowerCase();
		return _content.lastIndexOf(_words, fromIndex);
	}
}

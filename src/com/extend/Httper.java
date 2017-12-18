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
	 * 清除内容信息的标签
	 * @param content
	 * @return
	 */
	public static String clearTags(String content){
		if(content==null) return content;
		StringBuffer sb = new StringBuffer();
		StringBuffer tm = new StringBuffer();
		
		char c = 0, b=0;
		for(int i=0; i<content.length(); i++){
			c = content.charAt(i);
			if(c=='<' && b!='<' ){
				b = c; 
				continue;
			}
			if(c=='<' && b=='<' ){
				sb.append('<');
				sb.append(tm);
				tm.delete(0, tm.length());
				continue;
			}
			if(c=='>' && b=='<' ){
				tm.delete(0, tm.length());
				c = 9999; b=9999;
				continue;
			}
			if(b=='<'){
				tm.append(c);
			}else{
				sb.append(c);
			}
		}
		return sb.toString();
	}
	
	/**
	 * 剥离表格的内容信息(前提表格要正确的)
	 * @param columns
	 * @param content
	 * @return
	 */
	public static List<String[]> getTabVals(int columns,String content){
        String regex = "(?i)<tr[^>]*>" ;
        for(int i=0; i<columns; i++){
        	regex = regex + "\\s*<td[^>]*>([\\s\\S]*?)</td>";
        }
        regex = regex + "\\s*</tr>(?-i)";
        
        List<String[]> list = StringUtil.getGroups(content, regex);
        for(int j=0; j<list.size(); j++){
        	String[] arr = list.get(j);
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
	public static List<String[]> getTabVals(String content){
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
		int end   =  content.indexOf(endword, start+1);
		if(start<0 || end<0 || start>end || content==null ) return "";
		return content.substring(
				start + (include?0:startword.length()), 
				end   + (include?endword.length():0) );
	}
	public static String subString(String startword, String endword, String content){
		return subString(startword,endword,false,content);
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
		int end   =  _content.indexOf(_endword, start+1);
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
		int end   =  content.indexOf(endword, start+1);
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
		int end   =  _content.indexOf(_endword, start+1);
		if(start<0 || end<0 || start>end || content==null ) return content;
		String startStr = preclude?"":content.substring(0, start);
		return startStr + content.substring(end+endword.length());
	}
	public static String delstring(String startword,String endword, String content){
		return delstring(startword,endword, true , content);
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
	 * 删除被屏蔽代码
	 * @param content
	 * @return
	 */
	public static String deluseless(String detail){
		int len = detail.length(), tmp = 0;
		while(len != tmp){
			detail = Httper.delstring("<!--","-->", false, detail);
			tmp = len;
			len = detail.length();
		}
		return detail;
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
	
	/**
	 * 获取节点内容
	 * @param nodeword
	 * @param include
	 * @param content
	 * @return
	 */
	public static String getNodeVals(String nodeword, boolean include, String content){
		if(nodeword==null || content==null) return null;
		char p,c=0,cc;
		if(!nodeword.startsWith("<")) {
			int dex = indexof(content,nodeword);
			if(dex<1) return "";
			boolean test = false;
			StringBuffer newword = new StringBuffer(nodeword);
			for(int i=dex-1; i>=0; i--){
				c = content.charAt(i);
				newword.insert(0, c);
				if(c=='<'){
					test = true;
					break;
				}
				if(c=='>'){
					break;
				}
			}
			if(!test) return "";
			else nodeword = newword.toString();
		}
		boolean is = true, ko = true, begin=false ;
		StringBuffer head = new StringBuffer();
		StringBuffer last = new StringBuffer();
		StringBuffer node = new StringBuffer();
		StringBuffer text = new StringBuffer();
		for(int i=0; i<nodeword.length(); i++){
			c = nodeword.charAt(i);
			if(c==' '||c=='\t'||c=='\r'||c=='\n'||c=='>'){
				is = false;
			}
			if(is && i>0){
				node.append(c);
				if(i==nodeword.length()-1){
					is = false;
				}
			}
			if(ko && c=='>'){
				ko = false;
				head.append(c);
				continue;
			}
			if(ko){
				head.append(c);
			}else{
				text.append(c);
			}
		}
		if(node.length()<=0 || is==true) return "";
		last.append("</"+node+">");
		node=new StringBuffer("<"+node);
		
		if(ko==false && head.toString().endsWith("/>")){
			if(!include) return "";
			return head.toString();
		}
		
		boolean at = false;
		int orgsize = 0;
		int z=0,zlen=node.length();
		for(int i=0; i<nodeword.length(); i++){
			p = c;
			c = nodeword.charAt(i);
			cc = node.charAt(z);
			if(samechar(c,cc)){z++;}else{z=0;}
			
			if(at && c=='>'){
				if(p=='/'){
					orgsize --;
				   if(orgsize==0){
						if(!include) return "";
						return head.toString();
				   }
				}
				at = false;
			}
			
			if(z==zlen){
				z = 0;
				orgsize ++;
				at = true;
			}
		}
		
		if(orgsize<=0) return "";
		
		int r=0,rlen=nodeword.length();
		int l=0,llen=last.length();
		int n=0,nlen=node.length();
		int count = 0;
		char rr,ll,nn;
		at = false;
		for(int i=0; i<content.length(); i++){
			p = c;  c = content.charAt(i);
			
			if(begin){
				ll = last.charAt(l);
				nn = node.charAt(n);
				if(samechar(c,ll)){l++;}else{l=0;}
				if(samechar(c,nn)){n++;}else{n=0;}
			}else{
				rr = nodeword.charAt(r);
				if(samechar(c,rr)){r++;}else{r=0;}
			}
			
			if(r == rlen){
				r = 0;
				count = orgsize;
				begin = true;
				continue;
			}
			
			if(!begin)continue;
			
			if(ko){
				head.append(c);
				if(c=='>'){
					ko = false;
				}
			}else{
				text.append(c);
			}
			
			if(at && c=='>'){   //<a xxxx/>
				if(p=='/'){
				   count --;
				   if(count==0){
						if(!include) return "";
						return head.toString();
				   }
				}
				at = false;
			}
			
			if(l==llen){//xxx</a>
				count--;
				l = 0;
				if( count==0 ){
					text.delete(text.length()-last.length(), text.length());
					break;
				}else{
					continue;
				}
			}
			
			if(n==nlen){//<a>xxx<a
				n = 0;
				count ++;
				at = true;
			}
		}
		
		if(!begin || ko==true) return "";
		
		if(!include) return text.toString();
		else return head.toString()+text.toString()+last.toString();
	}
	public static String getNodeVals(String nodeword, String content){
		return getNodeVals(nodeword, false, content);
	}
	private static boolean samechar(char a, char b){
		return a-b==0 || a-b==32 || a-b==-32;
	}
	
}

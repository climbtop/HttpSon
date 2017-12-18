package com.common.extend.util;

public class NumberUtil {
	public static String toString(long num, long hex){
		StringBuffer sb = new StringBuffer();
		long mod,div;
		do{
			mod = num%hex;
			sb.append(getchar(mod));
			div = num/hex;
			if(div<=0)break;
			num = div;
		}while(true);
		return sb.reverse().toString();
	}
	private static char getchar(long c){
		if(c>=0 && c<10) return (char)('0'+c);
		else if(c>=10 && c<36 ) return (char)('a'+c-10);
		else return '-';
	}
	public static long parseInt(String str, long hex){
		long num = 0;
		if(str==null) return num;
		str = str.toLowerCase();
		for(int i=0; i<str.length(); i++){
			char c = str.charAt(i);
			long x = getlong(c);
			num = num*hex + x;
		}
		return num;
	}
	private static long getlong(char c){
		if(c>='0' && c<='9') return c-'0';
		else if(c>='a' && c<'z' ) return c-'a'+10;
		else return 0;
	}
}
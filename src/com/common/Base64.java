package com.common;

public class Base64 {

	public static String decode(String str) {
		return new String(java.util.Base64.getDecoder().decode(str));
	}

	public static String encode(String str) {
		if(str==null) return str;
		return new String(java.util.Base64.getEncoder().encode(str.getBytes()));
	}
	
	public static void main(String[] args) {
		String s = "bd80f1c3-d790-48e9-9186-ea6a348b2838:session";
		
		System.out.println(encode(s));
		System.out.println(decode(encode(s)));
	}
}

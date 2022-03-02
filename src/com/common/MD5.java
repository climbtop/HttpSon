package com.common;

import java.math.BigInteger;
import java.security.MessageDigest;

public class MD5 {
	static final char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
	static final char hexdigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	public static String encode(String plainText) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");

			md.update(plainText.getBytes());

			return new BigInteger(1, md.digest()).toString(16);
		} catch (Exception e) {
			return "";
		}
	}

	public static String ENCODE(String plainText) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");

			md.update(plainText.getBytes());

			byte[] mdResult = md.digest();
			int j = mdResult.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = mdResult[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(str);
		} catch (Exception e) {
			return "";
		}
	}

	public static String encode(String plainText, String saltValue) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");

			md.update(plainText.getBytes());
			md.update(saltValue.getBytes());

			return new BigInteger(1, md.digest()).toString(16);
		} catch (Exception e) {
			return "";
		}
	}

	public static String ENCODE(String plainText, String saltValue) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");

			md.update(plainText.getBytes());
			md.update(saltValue.getBytes());

			byte[] mdResult = md.digest();
			int j = mdResult.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = mdResult[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(str);
		} catch (Exception e) {
			return "";
		}
	}

	public final static String md5(String plainText) {
		try {
			MessageDigest mdTemp = MessageDigest.getInstance("MD5");

			mdTemp.update(plainText.getBytes("UTF-8"));

			byte[] md = mdTemp.digest();
			int j = md.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				str[k++] = hexdigits[byte0 >>> 4 & 0xf];
				str[k++] = hexdigits[byte0 & 0xf];
			}
			return new String(str);
		} catch (Exception e) {
			return "";
		}
	}

	public static boolean valid(String text, String md5) {
		return md5.equals(md5(text)) || md5.equals(md5(text).toUpperCase());
	}

}

package com.common;

/**
 * @Describe {} [] () <> "" ''
 */
public class TagUtil {
	
	private static Character[] startChars, stopChars;
	
	static {
		startChars = new Character[] { '{', '[', '(', '<', '"', '\'' };
		stopChars = new Character[] { '}', ']', ')', '>', '"', '\'' };
	}

	private static Character firstStartChar(String content) {
		if (content == null)
			return null;
		for (int i = 0; i < content.length(); i++) {
			Character searchChar = content.charAt(i);
			for (int j = 0; j < startChars.length; j++) {
				if (searchChar.equals(startChars[j])) {
					return searchChar;
				}
			}
		}
		return null;
	}

	private static Character stopChar(Character startChar) {
		if(startChar==null) return null;
		for (int j = 0; j < startChars.length; j++) {
			if (startChar.equals(startChars[j]) && j < stopChars.length) {
				return stopChars[j];
			}
		}
		return null;
	}

	public static String parseBody(String content) {
		return parseBody(content, firstStartChar(content));
	}

	public static String parseBody(String content, Character startChar) {
		StringBuffer sb = new StringBuffer();
		Character stopChar = stopChar(startChar);
		if (content == null || startChar == null || stopChar == null) {
			return sb.toString();
		}

		int counter = 0;
		Character prevChar = null, currChar = null;

		for (int i = 0; i < content.length(); i++) {
			prevChar = currChar;
			currChar = content.charAt(i);

			if (counter > 0) {
				if (prevChar != null && prevChar.equals('\\')) {
					sb.append(currChar);
					continue;
				}
			}

			if (currChar.equals(startChar)) {
				if (counter == 0 || !startChar.equals(stopChar)) {
					counter++;
					sb.append(currChar);
					continue;
				}
			}

			if (counter > 0) {
				if (currChar.equals(stopChar)) {
					counter--;
					sb.append(currChar);

					if (counter == 0) {
						return sb.toString();
					}

					continue;
				}
				sb.append(currChar);
			}
		}

		if (counter != 0) {
			sb.delete(0, sb.length());
		}
		return sb.toString();
	}

	public static String parseText(String content) {
		return parseText(content, firstStartChar(content));
	}

	public static String parseText(String content, Character startChar) {
		String body = parseBody(content, startChar);
		if (body != null && body.length() > 2) {
			return body.substring(1, body.length() - 1);
		}
		return body;
	}

}

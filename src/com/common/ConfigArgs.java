package com.common;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class ConfigArgs {

	public static String ARGS_CONFIG = "./args.txt";

	public static String[] readArgs(String[] args) {
		if (!new File(ARGS_CONFIG).exists()) {
			if (args.length > 0 && args[0].endsWith(ARGS_CONFIG.substring(1))) {
				if (new File(args[0]).exists()) {
					ARGS_CONFIG = args[0];
				}
			}
		}
		if (!new File(ARGS_CONFIG).exists())
			return args;
		List<String> argsList = new ArrayList<String>();
		try {
			StringBuffer b = new StringBuffer();
			Stack<Character> s = new Stack<Character>();
			Boolean marks = false;
			Reader r = new InputStreamReader(new FileInputStream(ARGS_CONFIG), "GBK");
			int t;
			while ((t = r.read()) != -1) {
				if (t == 65279)
					continue;
				char c = (char) t;
				if (c == '"') {
					s.push('"');
					if (s.size() == 2) {
						s.pop();
						s.pop();
						b.append('"');
					}
					if (b.length() == 0) {
						marks = true;
					}
				} else if ((c == ' ' || c == '\t') && s.size() == 0) {
					if (s.size() == 2) {
						s.pop();
						s.pop();
					}
					if (marks)
						b.deleteCharAt(b.length() - 1);
					if (marks || b.length() > 0) {
						argsList.add(b.toString());
					}
					b.delete(0, b.length());
					marks = false;
				} else {
					b.append(c);
				}
			}
			if (b.length() > 0) {
				if (marks)
					b.deleteCharAt(b.length() - 1);
				if (marks || b.length() > 0) {
					argsList.add(b.toString());
				}
			}
			r.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (argsList.size() == 0)
			return args;

		String[] newArgs = new String[argsList.size()];
		for (int i = 0; i < argsList.size(); i++) {
			newArgs[i] = argsList.get(i);
			System.out.print(newArgs[i]+" ");
		}
		System.out.println();
		return newArgs;
	}

}
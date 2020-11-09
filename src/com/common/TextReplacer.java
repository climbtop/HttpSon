package com.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

public class TextReplacer {

	public String replaceFileTmp = ".bak";
	public String replaceFileEnc = "UTF-8";
	private Set<String> designedExtSet = new HashSet<String>();

	public TextReplacer() {
		initDesignedExtSet();
	}
	
	public static void main(String[] args) throws Exception {
		TextReplacer fr = new TextReplacer();
		replace(fr, args);
	}
	
	public static void replace(TextReplacer fr, String[] args) throws Exception {
		fr.replaceFileEnc = (args.length > 3 ? args[3] : fr.replaceFileEnc);
		if (args.length > 2) {
			if (!new File(args[2]).exists()) {
				args[2] = "./" + args[2];
			}
			System.out.println(String.format("args: %s\t%s\t%s\t%s", args[0],
					args[1], args[2], fr.replaceFileEnc));
			fr.replaceFolder(args[0], args[1], args[2]);
		} else {
			System.out
					.println("missing args: oldWord, newWord, filePath, fileEnc");
		}
	}

	public void replaceFolder(String oldWord, String newWord, String filePath) throws Exception {
		File folder = new File(filePath);
		if (!folder.exists())
			return;
		if (folder.isFile()) {
			replaceFile(oldWord, newWord, filePath);
			return;
		}
		File[] files = folder.listFiles();
		for (File file : files) {
			try {
				if (file.isDirectory()) {
					replaceFolder(oldWord, newWord, file.getAbsolutePath());
				}
				if (file.isFile()) {
					replaceFile(oldWord, newWord, file.getAbsolutePath());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public boolean testValidFile(String oldWord, String newWord, String filePath) {
		File fileCur = new File(filePath);
		if (oldWord.length() <= 0 || !fileCur.exists())
			return false;
		String fileExt = fileCur.getName();
		if (fileExt.endsWith(replaceFileTmp)) {
			return false;
		}
		if (fileExt.indexOf(".") >= 0) {
			fileExt = fileExt.substring(fileExt.lastIndexOf(".")).toLowerCase();
			return designedExtSet.contains(fileExt);
		}
		return false;
	}

	public boolean replaceFile(String oldWord, String newWord, String filePath) throws Exception {
		if (!testValidFile(oldWord, newWord, filePath)) {
			return true;
		}
		String filePathBak = filePath + replaceFileTmp;
		String enc = replaceFileEnc;

		try {
			Reader fr = enc == null ? new FileReader(filePath)
					: new InputStreamReader(new FileInputStream(filePath), enc);

			Writer fw = enc == null ? new OutputStreamWriter(new FileOutputStream(filePathBak, true))
					: new OutputStreamWriter(new FileOutputStream(filePathBak, true), enc);

			PrintWriter printWriter = new PrintWriter(new BufferedWriter(fw));

			BufferedReader br = new BufferedReader(fr);
			String line = "";
			while ((line = br.readLine()) != null) {
				String text = line.trim();
				while (text.indexOf(oldWord) >= 0) {
					text = text.replace(oldWord, newWord);
				}
				printWriter.println(text);
			}
			br.close();
			fr.close();

			printWriter.flush();
			printWriter.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		new File(filePath).delete();
		new File(filePathBak).renameTo(new File(filePath));

		System.out.println(filePath + "\t" + ("[Done]"));
		return true;
	}

	private void initDesignedExtSet() {
		designedExtSet.add(".java");
	}

	public Set<String> getDesignedExtSet() {
		return designedExtSet;
	}

	public void setDesignedExtSet(Set<String> designedExtSet) {
		this.designedExtSet = designedExtSet;
	}

}

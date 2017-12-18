package com.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

public class FileReplacer {

	private String replaceFileOrg = ".rori";
	private String replaceFileTmp = ".rtmp";
	private String replaceFileEnc = "gbk";
	private Set<String> defaultExtSet = new HashSet<String>();

	public FileReplacer() {
		initDefaultExtSet();
	}

	public static void main(String[] args) throws Exception {
		FileReplacer fr = new FileReplacer();
		String replaceFileEnc = (args.length > 3 ? args[3] : fr.replaceFileEnc);
		if (args.length > 2) {
			if (!new File(args[2]).exists()) {
				args[2] = "./" + args[2];
			}
			System.out.println(String.format("args: %s\t%s\t%s\t%s", args[0],
					args[1], args[2], replaceFileEnc));
			fr.replaceFolder(args[0], args[1], args[2]);
		} else {
			System.out
					.println("missing args: oldWord, newWord, filePath, fileEnc");
		}
	}

	public void replaceFolder(String oldWord, String newWord, String filePath)
			throws Exception {
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
			replaceFileName(oldWord, newWord, file.getAbsolutePath());
		}
		replaceFileName(oldWord, newWord, filePath);
	}

	public void replaceFileName(String oldWord, String newWord, String filePath) {
		File oldFile = new File(filePath);
		if (!oldFile.exists())
			return;
		String oldName = oldFile.getName();
		if (oldName.indexOf(oldWord) >= 0) {
			String newName = oldName.replace(oldWord, newWord);
			if (!newName.equals(oldName)) {
				File newFile = new File(oldFile.getParent() + "/" + newName);
				oldFile.renameTo(newFile);
			}
		}
	}

	public boolean replaceFile(String oldWord, String newWord, String filePath)
			throws Exception {
		File fileCur = new File(filePath);
		if (!fileCur.exists())
			return true;
		String fileExt = fileCur.getName();
		if (fileExt.endsWith(replaceFileTmp)
				|| fileExt.endsWith(replaceFileOrg)) {
			System.out.println(filePath + "\t" + ("[IgnoredIt]"));
			return true;
		}
		if (fileExt.indexOf(".") >= 0) {
			fileExt = fileExt.substring(fileExt.lastIndexOf(".")).toLowerCase();
			if (defaultExtSet.contains(fileExt)) {
				System.out.println(filePath + "\t" + ("[IgnoredIt]"));
				return true;
			}
		}

		File fileTmp = new File(filePath + replaceFileTmp);
		File fileOrg = new File(filePath + replaceFileOrg);
		InputStream is = new FileInputStream(fileCur);
		OutputStream os = new FileOutputStream(fileTmp, false);
		ReplaceKit pkit = new ReplaceKit(is, os, oldWord, newWord,
				replaceFileEnc);
		boolean result = pkit.replaceStream();
		os.close();
		is.close();
		fileCur.renameTo(fileOrg);
		fileTmp.renameTo(fileCur);
		fileOrg.delete();

		System.out.println(filePath + "\t" + (result ? "[ReplaceIt]" : ""));
		return true;
	}

	private class ReplaceKit {
		int p = 0;
		InputStream is;
		OutputStream os;
		byte[] oldWordByte;
		byte[] newWordByte;
		private boolean hasMatch;

		public ReplaceKit(InputStream is, OutputStream os, String oldWord,
				String newWord, String enc) throws Exception {
			this.is = is;
			this.os = os;
			this.oldWordByte = oldWord.getBytes(enc);
			this.newWordByte = newWord.getBytes(enc);
			this.hasMatch = false;
		}

		public void test(int c) throws Exception {
			if ((oldWordByte[p] & 0xff) == c) {
				p++;
				if (p == oldWordByte.length) {
					for (byte b : newWordByte) {
						os.write((b & 0xff));
					}
					p = 0;
					hasMatch = true;
				}
			} else {
				if (p != 0) {
					int m = p;
					p = 0;
					os.write(oldWordByte[0] & 0xff);
					for (int i = 1; i < m; i++) {
						test(oldWordByte[i] & 0xff);
					}
					test(c);
				} else {
					p = 0;
					os.write(c);
				}
			}
		}

		public boolean replaceStream() throws Exception {
			int c;
			while ((c = is.read()) != -1) {
				test(c);
			}
			if (p != 0) {
				for (int i = 0; i < p; i++) {
					os.write(oldWordByte[i] & 0xff);
				}
			}
			return isHasMatch();
		}

		public boolean isHasMatch() {
			return hasMatch;
		}
	}

	public Set<String> getDefaultExtSet() {
		return defaultExtSet;
	}

	public void setDefaultExtSet(Set<String> defaultExtSet) {
		this.defaultExtSet = defaultExtSet;
	}

	public String getReplaceFileOrg() {
		return replaceFileOrg;
	}

	public void setReplaceFileOrg(String replaceFileOrg) {
		this.replaceFileOrg = replaceFileOrg;
	}

	public String getReplaceFileTmp() {
		return replaceFileTmp;
	}

	public void setReplaceFileTmp(String replaceFileTmp) {
		this.replaceFileTmp = replaceFileTmp;
	}

	public String getReplaceFileEnc() {
		return replaceFileEnc;
	}

	public void setReplaceFileEnc(String replaceFileEnc) {
		this.replaceFileEnc = replaceFileEnc;
	}

	private void initDefaultExtSet() {
		defaultExtSet.add(".jpg");
		defaultExtSet.add(".png");
		defaultExtSet.add(".gif");
		defaultExtSet.add(".jpeg");
		defaultExtSet.add(".bmp");
		defaultExtSet.add(".xls");
		defaultExtSet.add(".xlsx");
		defaultExtSet.add(".jar");
		defaultExtSet.add(".war");
		defaultExtSet.add(".rar");
		defaultExtSet.add(".zip");
		defaultExtSet.add(".7z");
		defaultExtSet.add(".iso");
		defaultExtSet.add(".bin");
		defaultExtSet.add(".c");
		defaultExtSet.add(".h");
		defaultExtSet.add(".cpp");
		defaultExtSet.add(".z");
		defaultExtSet.add(".o");
		defaultExtSet.add(".so");
		defaultExtSet.add(".tar");
		defaultExtSet.add(".gz");
		defaultExtSet.add(".bz2");
		defaultExtSet.add(".bz");
		defaultExtSet.add(".rpm");
		defaultExtSet.add(".deb");
		defaultExtSet.add(".log");
	}

}

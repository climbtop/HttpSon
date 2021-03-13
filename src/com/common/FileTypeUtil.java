package com.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileTypeUtil {

	public static final String DOS = "dos";
	public static final String UNIX = "unix";
	public static final String MAC = "mac";

	private static boolean checked(String filePath) {
		if (filePath == null || filePath.trim().length() == 0) {
			return false;
		}
		File file = new File(filePath);
		return file.isFile() && file.exists() && file.canWrite();
	}

	private static String getTmpFile(String filePath) {
		return filePath + ".nff";
	}

	public static boolean toDos(String filePath) {
		return toConvertType(DOS, filePath);
	}

	public static boolean toUnix(String filePath) {
		return toConvertType(UNIX, filePath);
	}

	public static boolean toMac(String filePath) {
		return toConvertType(MAC, filePath);
	}

	private static boolean toConvertType(String type, String filePath) {
		if (!checked(filePath)) {
			return false;
		}
		filePath = filePath.trim();
		String newFilePath = getTmpFile(filePath);

		FileInputStream fin = null;
		FileOutputStream fout = null;
		try {
			fin = new FileInputStream(filePath);
			fout = new FileOutputStream(newFilePath, false);

			int ch;
			while ((ch = fin.read()) != -1) {
				if (toConvert(type, ch, fin, fout)) {
					break;
				}
			}

		} catch (Exception e) {
		} finally {
			if (fin != null) {
				try {
					fin.close();
				} catch (Exception e) {
				}
			}
			if (fout != null) {
				try {
					fout.close();
				} catch (Exception e) {
				}
			}
		}
		return toRename(filePath, newFilePath);
	}

	private static boolean toConvert(String type, int ch, FileInputStream fin, FileOutputStream fout)
			throws IOException {
		if (ch == '\n') {
			toPrintln(type, fout);
		} else if (ch == '\r') {
			int dh = fin.read();
			if (dh == -1) {
				toPrintln(type, fout);
				return true;
			} else if (dh == '\n') {
				toPrintln(type, fout);
			} else {
				toPrintln(type, fout);
				fout.write(dh);
			}
		} else {
			fout.write(ch);
		}
		return false;
	}

	private static void toPrintln(String type, FileOutputStream fout) throws IOException {
		if (DOS.equals(type)) {
			fout.write('\r');
			fout.write('\n');
		} else if (UNIX.equals(type)) {
			fout.write('\n');
		} else if (MAC.equals(type)) {
			fout.write('\r');
		}
	}

	private static boolean toRename(String filePath, String newFilePath) {
		if (!checked(newFilePath)) {
			return false;
		}
		if (checked(filePath)) {
			new File(filePath).delete();
		}
		return new File(newFilePath).renameTo(new File(filePath));
	}

}

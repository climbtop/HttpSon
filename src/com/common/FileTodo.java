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
import java.util.function.Consumer;
import java.util.function.Function;

public class FileTodo {

	public static String GBK = "GBK";
	public static String UTF8 = "UTF-8";

	public static void readFile(String filePath, String fileEnc, Consumer<String> consumer) {
		if (!new File(filePath).exists())
			return;
		try {
			Reader fr = fileEnc == null ? new FileReader(filePath)
					: new InputStreamReader(new FileInputStream(filePath), fileEnc);

			BufferedReader br = new BufferedReader(fr);
			String line = "";
			while ((line = br.readLine()) != null) {
				if (line.length() > 0) {
					consumer.accept(line);
				}
			}
			br.close();
			fr.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void writeFile(String sourcePath, String targetPath, String fileEnc, Function<String, String> function) {
		if (!new File(sourcePath).exists())
			return;
		try {
			final Writer fw = (fileEnc == null) ? new OutputStreamWriter(new FileOutputStream(targetPath, true))
					: new OutputStreamWriter(new FileOutputStream(targetPath, true), fileEnc);

			final PrintWriter printWriter = new PrintWriter(new BufferedWriter(fw));

			readFile(sourcePath, fileEnc, line -> {
				try {
					String result = function.apply(line);
					if (result != null) {
						printWriter.println(result);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			
			printWriter.flush();
			printWriter.close();
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

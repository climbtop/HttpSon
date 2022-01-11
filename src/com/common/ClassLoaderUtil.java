package com.common;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class ClassLoaderUtil {

	public static void loadJar(String jarPath) throws Exception {
		File jarFile = new File(jarPath);
		Method method = null;
		try {
			method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
		} catch (NoSuchMethodException | SecurityException e1) {
			throw e1;
		}
		boolean accessible = method.isAccessible();
		try {
			method.setAccessible(true);
			URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
			URL url = jarFile.toURI().toURL();
			method.invoke(classLoader, url);
		} catch (Exception e) {
			throw e;
		} finally {
			method.setAccessible(accessible);
		}
	}

	@SuppressWarnings("unused")
	private static void addDLLDir(String libraryPath) throws Exception {
		try {
			Field userPathsField = ClassLoader.class.getDeclaredField("usr_paths");
			userPathsField.setAccessible(true);
			String[] paths = (String[]) userPathsField.get(null);
			StringBuilder sb = new StringBuilder();
			String split = ("/".equals(File.separator) ? ":" : ";");
			for (int i = 0; i < paths.length; i++) {
				if (libraryPath.equals(paths[i])) {
					continue;
				}
				sb.append(paths[i]).append(split);
			}
			sb.append(libraryPath);
			// java.library.path, Like: -Djava.library.path={libraryPath}
			System.setProperty("java.library.path", sb.toString());
			final Field sysPathsField = ClassLoader.class.getDeclaredField("sys_paths");
			sysPathsField.setAccessible(true);
			// set sys_pathsnull
			sysPathsField.set(null, null);
		} catch (Exception e) {
			throw e;
		}
	}
	
}

package com.common;

import java.io.File;
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

}

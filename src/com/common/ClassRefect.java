package com.common;

import java.io.File;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassRefect {
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static ArrayList<Class> getAllClassByInterface(Class clazz) {
		ArrayList<Class> list = new ArrayList<>();
		if (clazz.isInterface()) {
			try {
				ArrayList<Class> allClass = getAllClass(clazz.getPackage().getName());
				for (int i = 0; i < allClass.size(); i++) {
					Class current = allClass.get(i);
					boolean isAbstract = Modifier.isAbstract(current.getModifiers());
					if (clazz.isAssignableFrom(current)) {
						if (!clazz.equals(current) && !isAbstract) {
							list.add(current);
						}
					}
				}
			} catch (Exception e) {
			}
		}
		return list;
	}

	@SuppressWarnings("rawtypes")
	private static ArrayList<Class> getAllClass(String packagename) {
		List<String> classNameList = getClassName(packagename);
		
		for(int i=classNameList.size()-1; i>=0; i--) {
			String className = classNameList.get(i);
			if(!className.startsWith(packagename)) {
				classNameList.remove(i);
			}
		}

		ArrayList<Class> list = new ArrayList<>();

		for (String className : classNameList) {
			try {
				list.add(Class.forName(className));
			} catch (Exception e) {
			}
		}
		return list;
	}

	public static List<String> getClassName(String packageName) {
		List<String> fileNames = null;
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		String packagePath = packageName.replace(".", "/");
		URL url = loader.getResource(packagePath);
		if (url != null) {
			String type = url.getProtocol();
			if (type.equals("file")) {
				String fileSearchPath = url.getPath();
				fileSearchPath = fileSearchPath.substring(0, fileSearchPath.indexOf("/classes"));
				fileNames = getClassNameByFile(fileSearchPath);
			} else if (type.equals("jar")) {
				try {
					JarURLConnection jarURLConnection = (JarURLConnection) url.openConnection();
					JarFile jarFile = jarURLConnection.getJarFile();
					fileNames = getClassNameByJar(jarFile, packagePath);
				} catch (java.io.IOException e) {
				}

			} else {
			}
		}
		return fileNames;
	}

	private static List<String> getClassNameByFile(String filePath) {
		List<String> myClassName = new ArrayList<String>();
		try {
			File file = new File(filePath);
			File[] childFiles = file.listFiles();
			for (File childFile : childFiles) {
				if (childFile.isDirectory()) {
					myClassName.addAll(getClassNameByFile(childFile.getPath()));
				} else {
					String childFilePath = childFile.getPath();
					if (childFilePath.endsWith(".class")) {
						if(childFilePath.indexOf("\\")>=0) {
							childFilePath = childFilePath.substring(childFilePath.indexOf("\\classes") + 9,
									childFilePath.lastIndexOf("."));
							childFilePath = childFilePath.replace("\\", ".");
						}else {
							childFilePath = childFilePath.substring(childFilePath.indexOf("/classes") + 9,
									childFilePath.lastIndexOf("."));
							childFilePath = childFilePath.replace("/", ".");
						}
						myClassName.add(childFilePath);
					}
				}
			}
		} catch (Exception e) {
		}
		return myClassName;
	}

	private static List<String> getClassNameByJar(JarFile jarFile, String packagePath) {
		List<String> myClassName = new ArrayList<String>();
		try {
			Enumeration<JarEntry> entrys = jarFile.entries();
			while (entrys.hasMoreElements()) {
				JarEntry jarEntry = entrys.nextElement();
				String entryName = jarEntry.getName();
				if (entryName.endsWith(".class")) {
					entryName = entryName.replace("/", ".").substring(0, entryName.lastIndexOf("."));
					myClassName.add(entryName);
				}
			}
		} catch (Exception e) {
		}
		return myClassName;
	}
}

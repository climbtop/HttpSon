package com.common;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URLDecoder;


public class LibraryReload {

	/**
	 * 加载本地目录classes,path
	 */
	public static void reload(){
		try{
			String sep = System.getProperty("file.separator");
			String pathroot = LibraryReload.class.getResource("").getFile().toString();
			pathroot = URLDecoder.decode(pathroot);
			pathroot = pathroot.replace("/", sep);
			pathroot = pathroot.replace("\\", sep);
			pathroot = pathroot.substring(0, pathroot.toLowerCase().indexOf("classes"+sep));
			String classes = pathroot + "classes" +sep;
			String libpath = pathroot + "lib" +sep;
			addDir(classes);
			addDir(libpath);
		}catch(Exception e){
		}
	}
	
	/**
	 * 加载本地目录classes,path
	 */
	public static void reload(String libpath){
		try{
			addDir(libpath);
		}catch(Exception e){
		}
	}
	
	/**
	 * 新添加path;
	 * @param s
	 * @throws IOException
	 */
	public static void addDir(String s) throws IOException {   
	    try {   
	        Field field = ClassLoader.class.getDeclaredField("usr_paths");   
	        field.setAccessible(true);   
	        String[] paths = (String[])field.get(null);   
	        for (int i = 0; i < paths.length; i++) {   
	            if (s.equals(paths[i])) {   
	                return;   
	            }   
	        }   
	        String[] tmp = new String[paths.length+1];   
	        System.arraycopy(paths,0,tmp,0,paths.length);   
	        tmp[paths.length] = s;   
	        field.set(null,tmp);   
	    } catch (IllegalAccessException e) {   
	        throw new IOException("Failed to get permissions to set library path");   
	    } catch (NoSuchFieldException e) {   
	        throw new IOException("Failed to get field handle to set library path");   
	    }   
	}  
	
	/**
	 * 结果打印显示
	 */
	public static void print() {
		System.getProperties().list(System.out);
	}
	
}

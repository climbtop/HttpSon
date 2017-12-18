package com.common;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * 关于文件的工具类
 */
public class FileUtil {
	
	/**
	 * 查找相同目录下的文件,按照YYYY.1234834241979.xml格式时间序号返回.
	 * @param localFile
	 */
	public  static LinkedList<String> findDirectoryFiles(String localFile) {
		String filePath = localFile;
		if(new File(localFile).isFile()){
			filePath = localFile.substring(0,localFile.lastIndexOf("/")+1);
		}
		
		 LinkedList<String> fileList = new LinkedList<String> ();
		 File files = new File(filePath);
		 if(files.exists()==false) 
			 return fileList;
		 
		 Map<String,String> namePathMap = new HashMap<String,String>();
		 for(File file : files.listFiles()){
			 if(file.isFile()){
					String name = file.getName();
					String path = file.getAbsolutePath();
					namePathMap.put(name, path);
			 }
		 }
		 return sortFilesByKey(namePathMap);
	}
	
	/**
	 * 从文件获取KEY
	 * @param name
	 * @return
	 */
	public  static Long getKeyFromFileName(String name){
		Long key = 0L;
		int start = name.indexOf(".");
		int end   = name.lastIndexOf(".");
		if(start>=0 && end>=0 && end>start ){
			String index = name.substring(name.indexOf(".")+1, name.lastIndexOf("."));
			try{
				key = Long.valueOf(index);
			}catch(Exception e){
				key = 0L;
			}
		}
		return key;
	}
	
	/**
	 * 率选(文件名-文件路径)，按照特定格式序号返回.
	 * @param namePathMap
	 * @return
	 */
	public  static LinkedList<String> sortFilesByKey(Map<String,String> namePathMap){
		LinkedList<String> fileList = new LinkedList<String> ();
		Map<Long,String> keyPath = new HashMap<Long,String>();
		List<Long> keyList = new ArrayList<Long>();
		
		Iterator<String> iterator = namePathMap.keySet().iterator();
		while (iterator.hasNext()){
			String fileName = iterator.next();
			String filePath = namePathMap.get(fileName);

			Long key = getKeyFromFileName(fileName);
			if(key>0){
				keyList.add(key);
				keyPath.put(key, filePath);
			}
		}
		
		Object[] keyArray = keyList.toArray();
		Arrays.sort(keyArray);
		for(Object temp : keyArray ){
			Long key = (Long)temp;
			String path = keyPath.put(key, keyPath.get(key));
			fileList.addLast(path);
		}
		return fileList;
	}
	
	/**
	 * 返回指定目录下含有特定格式序号文件名列表
	 * @param localFile
	 * @return
	 */
	public  static HashSet<String> hashSetDirectoryFiles(String localFile) {
		LinkedList<String> linkedList = findDirectoryFiles(localFile);
		List<String> fileNameList = new ArrayList<String>();
		while(linkedList.size()>0){
			String filePath = linkedList.removeFirst();
			String fileName = getFileName(filePath);
			fileNameList.add(fileName);
		}
		return new HashSet<String>(fileNameList);
	}
	
	
	/**
	 * 从文件绝对路径取出文件名
	 */
	public  static String getFileName(String localFile) {
		if(localFile==null) return "";
		localFile = localFile.replace("\\","/");
		int start = localFile.lastIndexOf("/");
		if(start >=0 ){
			return localFile.substring(start+1);
		}
		return "";
	}
	
	
	/**
	 * 读取LocalFile From File
	 * @param filePath
	 * @return
	 */
	public static LinkedList<String> readFile(String filePath, String enc){
		LinkedList<String> localFileList = new LinkedList<String>();
		if(!new File(filePath).exists()) return localFileList;
		try{
			Reader fr = enc == null ?
					new FileReader(filePath) :
					new InputStreamReader (new FileInputStream(filePath),enc);

			BufferedReader br = new BufferedReader(fr);
			String line = "";
			while((line=br.readLine())!=null){
				if(line.trim().length()>0){
					localFileList.addLast(line.trim());
				}
			}
			br.close();
			fr.close();
		}catch(Exception e){
			//InputStream in = new FileInputStream(new File(""));
			//Reader isr = new InputStreamReader(in,"GBK");
			//BufferedReader br = new BufferedReader(isr);
		}
		return localFileList;
	}
	public static LinkedList<String> readFile(String filePath){
		return readFile(filePath, null);
	}
	public static List<String> readLocalFile(String filePath, String enc){
		LinkedList<String> localFileList = readFile(filePath, enc);
		return new ArrayList<String>(new HashSet<String>(localFileList));
	}
	public static List<String> readLocalFile(String filePath){
		return readLocalFile(filePath, null);
	}
	/**
	 * 读取LocalFile to Map
	 * @param filePath
	 * @return
	 */
	public static HashSet<String> readLocalFileToMap(String filePath){
		return new HashSet<String>(readLocalFile(filePath));
	}
	/**
	 * 读取LocalFile to Byte
	 * @param filePath
	 * @param enc
	 * @return
	 */
	public static byte[] readLocalFileToByte(String filePath){
		byte[] data = null;   
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(filePath);
            ByteArrayOutputStream bStrm = new ByteArrayOutputStream();   
            int ch;   
            while ((ch = fin.read()) != -1){   
                bStrm.write(ch);   
            }
			fin.close();
			data = bStrm.toByteArray();   
            bStrm.close(); 
		}catch(Exception e){
        }finally{
        	if(fin!=null) {
        		try{fin.close();}catch(Exception e){}
        	}
        }
		return data;
	}
	/**
	 * 读取LocalFile to String
	 * @param filePath
	 * @param enc
	 * @return
	 */
	public static String readLocalFileToString(String filePath,String enc){
		try {
			return new String(readLocalFileToByte(filePath),enc);
		} catch (Exception e) {
		}
		return null;
	}
	
	
	/**
	 * 保存LocalFile To File
	 * @param confirmLink
	 * @param flag
	 */
	public static void writeFile(String filePath, List<String> localFileList, String enc){
		try {
			Writer fw = enc == null ?
					new OutputStreamWriter (new FileOutputStream(filePath,true)) :
					new OutputStreamWriter (new FileOutputStream(filePath,true),enc);
			
			PrintWriter printWriter = new PrintWriter(new BufferedWriter(fw));
			for(String localFile : localFileList){
				printWriter.println(localFile);
			}
			printWriter.flush();
			printWriter.close();
		} catch (Exception e) {

		}
	}
	public static void writeFile(String filePath, List<String> localFileList){
		writeFile(filePath, localFileList, null);
	}
	public static void writeLocalFile(String filePath, List<String> localFileList, String enc){
		localFileList = new ArrayList<String>(new HashSet<String>(localFileList));
		writeFile(filePath, localFileList, enc);
	}
	public static void writeLocalFile(String filePath, List<String> localFileList){
		writeLocalFile(filePath, localFileList, null);
	}
	
	public static void writeLocalFileForByte(String filePath, byte[] data, boolean append){
		ByteArrayInputStream iStrm = null;  
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(filePath,append);  
            iStrm = new ByteArrayInputStream(data);
            int ch;   
            while ((ch = iStrm.read()) != -1){   
            	fout.write(ch);   
            }
            fout.close();
            iStrm.close();
		}catch(Exception e){
        }finally{
        	if(fout!=null) {
        		try{fout.close();}catch(Exception e){}
        	}
        }
	}
	public static void writeLocalFileForByte(String filePath, byte[] data){
		writeLocalFileForByte(filePath, data, false);
	}
	public static void writeLocalFileForString(String filePath, String data,String enc){
		try {
			writeLocalFileForByte(filePath, data.getBytes(enc), false);
		} catch (Exception e) {
		}
	}
	
	/**
	 * 采用缓冲方式写到文件
	 * @param filePath
	 * @param localFileList
	 * @param cacheSize
	 */
	public static void writeFileCache(String filePath, LinkedList<String> localFileList, int cacheSize){
		if(localFileList.size()<cacheSize)return;
		String lineSeparator = System.getProperty("line.separator");
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<localFileList.size(); i++){
			sb.append(localFileList.get(i));
			if(i<localFileList.size()-1){
				sb.append(lineSeparator);
			}
		}
		writeFile(filePath, sb.toString());
		localFileList.clear();
	}
	public static void writeFileFlush(String filePath, LinkedList<String> localFileList){
		writeFileCache(filePath,localFileList,0);
	}
	
	/**
	 * 清除LocalFile File
	 * @param filePath
	 */
	public static void clearLocalFile(String filePath){
		try {
			PrintWriter printWriter = new PrintWriter(
					 new BufferedOutputStream (new FileOutputStream (new File(filePath))));
			printWriter.flush();
			printWriter.close();
		} catch (FileNotFoundException e) {
			
		}
	}
	
	/**
	 * 保存LocalFile To File
	 * @param confirmLink
	 * @param flag
	 */
	public static void writeFile(String filePath, String value, String enc){
		List<String> localFileList = new ArrayList<String>();
		localFileList.add(value);
		writeFile(filePath, localFileList, enc);
	}
	public static void writeLocalFile(String filePath, String value, String enc){
		writeFile(filePath, value, enc);
	}
	public static void writeFile(String filePath, String value){
		writeFile(filePath, value, null);
	}
	public static void writeLocalFile(String filePath, String value){
		writeLocalFile(filePath, value, null);
	}
	
	/**
	 * 读取本地资源流
	 * @param name
	 * @return
	 */
	public static InputStream getResourceAsStream(String name) {
		InputStream is = null;
		try {
			if (new File(name).exists()) {
				is = new FileInputStream(name);
			} else {
				is = FileUtil.class.getClassLoader().getResourceAsStream(name);
				if (is == null) {
					is = FileUtil.class.getResourceAsStream(name);
				}
				if (is == null) {
					if (new File("." + File.separator + name).exists()) {
						is = new FileInputStream("." + File.separator + name);
					}
				}
				if (is == null) {
				}
			}
		} catch (Exception e) {
		}
		return is;
	}	
	
	/**
	 * 串行化读出实例
	 * @param stream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Object readObject(String objectFile){
		Object object 	= null;
		try{
			ObjectInputStream stream = new ObjectInputStream(new 
					FileInputStream(objectFile)); 
			object 	= (Object) stream.readObject();
			stream.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return object;
	}
	
	/**
	 * 串行化保存实例
	 * @param stream
	 * @throws IOException
	 */
	public static void writeObject(String objectFile,Object object){
		try{
			ObjectOutputStream stream = new ObjectOutputStream(new 
					FileOutputStream(objectFile,false)); 
		    stream.writeObject(object);	
		    stream.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * 清空文件
	 */
	public static void empty(String filePath) {
		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream(filePath, false);
			fout.close();
		} catch (Exception e) {
		} finally {
			if (fout != null) {
				try {
					fout.close();
				} catch (Exception e) {
				}
			}
		}
	}

	/**
	 * 判断文件是否为空
	 * @param filePath
	 * @return
	 */
	public static boolean isEmpty(String filePath) {
		if (filePath == null || !new File(filePath).exists()) {
			return true;
		}
		return new File(filePath).length() == 0;
	}
}

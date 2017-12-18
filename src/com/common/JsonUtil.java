package com.common;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class JsonUtil {

	public static Object getProperty(String content, String key){
		return getProperty(content, key, null);
	}
	
	public static Object getProperty(String content, String key, Object defaultValue){
		if(key==null || content==null) return defaultValue;
		try{
			if(key.startsWith("[")){
				String dexStr = key.substring( key.indexOf('[')+1, key.indexOf(']'));
				key = key.substring(key.indexOf(']')+1).trim();
				
				List list = new LinkedList();
				JSONArray array = JSONArray.fromObject(content);
				for(int i=0; i<array.size(); i++){
					list.add( getProperty(array.getJSONObject(i), key) );
				}
				if(dexStr!=null && dexStr.length()>0 && dexStr.trim().matches("\\d+")){
					int index = Integer.parseInt(dexStr.trim());
					if(index>=0 && index<list.size()){
						return list.get(index);
					}
					return null;
				}
				return list;
			}else{
				JSONObject jo = JSONObject.fromObject(content);
				return getProperty(jo, key);
			}
		}catch(Exception e){
		}
		return defaultValue;
	}
	
	private static Object getProperty(JSONObject jo, String key){
		String name = key;
		int dot = key.indexOf(".");
		if(dot>0){
			name = key.substring(0,dot);
			key = key.substring(dot+1);
		}else{
			key = "";
		}
		if(name.endsWith("]")){
			String dexStr = name.substring( name.lastIndexOf('[')+1, name.lastIndexOf(']'));
			name = name.substring(0, name.lastIndexOf('[')).trim();
			
			if(key.length()<=0){
				List list = new LinkedList();
				JSONArray array = jo.getJSONArray(name);
				for(int i=0; i<array.size(); i++){
					list.add( toBean(array.get(i)) );
				}
				if(dexStr!=null && dexStr.length()>0 && dexStr.trim().matches("\\d+")){
					int index = Integer.parseInt(dexStr.trim());
					if(index>=0 && index<list.size()){
						return list.get(index);
					}
					return null;
				}
				return list;
			}else{
				List list = new LinkedList();
				JSONArray array = jo.getJSONArray(name);
				for(int i=0; i<array.size(); i++){
					list.add( getProperty(array.getJSONObject(i), key) );
				}
				if(dexStr!=null && dexStr.length()>0 && dexStr.trim().matches("\\d+")){
					int index = Integer.parseInt(dexStr.trim());
					if(index>=0 && index<list.size()){
						return list.get(index);
					}
					return null;
				}
				return list;
			}
		}else{
			if(key.length()<=0){
				return toBean( jo.get(name) );
			}else{
				return getProperty(jo.getJSONObject(name), key);
			}
		}
	}
	
	private static Object toBean(Object obj){
		if(obj==null) return obj;
		if(obj instanceof JSONObject){
			JSONObject bean = (JSONObject)obj;
			Map map = new LinkedHashMap();
			for(Object key : bean.keySet()){
				Object val = bean.get(key);
				if(val instanceof JSONObject){
					map.put(key, toBean(val));
				}
				else if(val instanceof JSONArray){
					map.put(key, toBean(val));
				}
				else {
					map.put(key, val);
				}
			}
			return map;
		}
		else if(obj instanceof JSONArray){
			JSONArray array = (JSONArray)obj;
			List list = new LinkedList();
			for(int i=0; i<array.size(); i++){
				Object val = array.get(i);
				if(val instanceof JSONObject){
					list.add(toBean(val));
				}
				else if(val instanceof JSONArray){
					list.add(toBean(val));
				}
				else {
					list.add(val);
				}
			}
			return list;
		}
		return obj;
	}
	
}
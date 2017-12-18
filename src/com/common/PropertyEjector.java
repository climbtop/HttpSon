package com.common;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * key format 
 * key1.key2 
 * key1.key2[].key3   = key1.key2.[].key3
 * key1.key2[0].key3  = key1.key2.[0].key3
 * 
 */
public class PropertyEjector {

	private Object jo;

	public PropertyEjector(Object jo) {
		this.jo = jo;
	}

	public Object get(String keys) {
		return get(jo, keys, null);
	}

	public Object get(String keys, Object defaultValue) {
		return get(jo, keys, defaultValue);
	}

	private static class EjectorList<T> extends LinkedList<T> {
	};

	public Object get(Object jo, String keys, Object defaultValue) {
		if (keys == null || jo == null)
			return defaultValue;
		try {
			Object val = getValueOf(jo, keys);
			if (val != null && val instanceof EjectorList) {
				List<Object> list = new LinkedList<Object>();
				toOneLevel(list, (EjectorList<Object>)val );
				val = list;
			}
			return val;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return defaultValue;
	}

	private void toOneLevel(List<Object> list, EjectorList<Object> data){
		if(list ==null || data==null) return;
		for(Object obj : data){
			if(obj instanceof EjectorList){
				toOneLevel(list, (EjectorList<Object>)obj);
			}else{
				list.add(obj);
			}
		}
	}
	
	private void toNewLevel(List<Object> list, EjectorList<Object> data){
		if(list ==null || data==null) return;
		for(Object obj : data){
			if(obj instanceof EjectorList){
				List<Object> newList = new LinkedList<Object>();
				toNewLevel(newList, (EjectorList<Object>)obj);
				list.add(newList);
			}else{
				list.add(obj);
			}
		}
	}
	
	private Object getListOf(Object newJo, String dexStr, String keys) {
		if (newJo == null)
			return newJo;
		List<Object> list = new EjectorList<Object>();
		if (newJo instanceof Iterable) {
			Iterable<Object> array = (Iterable<Object>) newJo;
			for (Object obj : array) {
				Object val = getValueOf(obj, keys);
				if(val!=null){
					list.add(val);
				}
			}
		} else if (newJo instanceof Map) {
			Map<Object, Object> map = (Map<Object, Object>) newJo;
			for (Object obj : map.values()) {
				Object val = getValueOf(obj, keys);
				if(val!=null){
					list.add(val);
				}
			}
		} else {
			return null;
		}
		if (dexStr.length() > 0) {
			newJo = null;
			if (dexStr != null && dexStr.length() > 0 && dexStr.trim().matches("\\d+")) {
				int index = Integer.parseInt(dexStr.trim());
				if (index >= 0 && index < list.size()) {
					newJo = list.get(index);
				}
			}
		} else {
			newJo = list;
		}
		return newJo;
	}

	private Object getMapOf(Object newJo, String name, String keys) {
		if (newJo == null)
			return newJo;
		if (newJo instanceof Map) {
			Map<Object, Object> map = (Map<Object, Object>) newJo;
			newJo = map.get(name);
		} else {
			try{
				String methodName = "get" +
					String.valueOf(name.charAt(0)).toUpperCase() + name.substring(1);
				Method m = newJo.getClass().getMethod(methodName);
				newJo = m.invoke(newJo);
			}catch(Exception e){
				newJo = null;
			}
		}
		if (keys.length() <= 0) {
			return toBean(newJo);
		} else {
			return getValueOf(newJo, keys);
		}
	}
	
	private Object getValueOf(Object jo, String keys) {
		if (jo == null || keys == null || keys.length() <= 0)
			return jo;
		String name = keys.trim();
		int dot = keys.indexOf(".");
		if (dot > 0) {
			name = keys.substring(0, dot).trim();
			keys = keys.substring(dot + 1).trim();
		} else {
			keys = "";
		}

		String dexStr = null;
		Object newJo = null;

		if (name.endsWith("]")) {
			dexStr = name.substring(name.lastIndexOf('[') + 1, name.lastIndexOf(']')).trim();
			name = name.substring(0, name.lastIndexOf('[')).trim();
		}

		if (name.length() > 0 && jo != null) {
			if (jo instanceof EjectorList) {
				List<Object> array = (EjectorList<Object>) jo;
				for (int i = 0; i < array.size(); i++) {
					Object val = getMapOf(array.get(i), name, keys);
					array.set(i, val);
				}
				newJo = jo;
			} else if (jo instanceof Map) {
				if(dexStr != null){
					newJo = getMapOf(jo, name, "["+dexStr+"]." + keys);
				}else{
					newJo = getMapOf(jo, name, keys);
				}
			} else {
				newJo = null;
			}
			return newJo;
		}
		
		if (dexStr != null && jo != null) {
			if (jo instanceof EjectorList) {
				List<Object> array = (EjectorList<Object>) jo;
				for (int i = 0; i < array.size(); i++) {
					Object val = getListOf(array.get(i), dexStr, keys);
					array.set(i, val);
				}
				newJo = jo;
			} else if (jo instanceof Iterable || jo instanceof Map) {
				newJo = getListOf(jo, dexStr, keys);
			} else {
				newJo = null;
			}
			return newJo;
		}

		return newJo;
	}

	private Object toBean(Object obj) {
		if (obj == null)
			return obj;
		if (obj instanceof Map) {
			Map<Object, Object> bean = (Map<Object, Object>) obj;
			Map<Object, Object> map = new LinkedHashMap<Object, Object>();
			for (Object key : bean.keySet()) {
				Object val = bean.get(key);
				if (val instanceof Map) {
					map.put(key, toBean(val));
				} else if (val instanceof List) {
					map.put(key, toBean(val));
				} else {
					map.put(key, val);
				}
			}
			return map;
		} else if (obj instanceof Iterable) {
			List<Object> list = new LinkedList<Object>();
			Iterable<Object> array = (Iterable<Object>) obj;
			for (Object val : array) {
				if (val instanceof Map) {
					list.add(toBean(val));
				} else if (val instanceof List) {
					list.add(toBean(val));
				} else {
					list.add(val);
				}
			}
			return list;
		}
		return obj;
	}

}
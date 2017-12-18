package com.common;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FilterMapUtil {

	public static Object filter(Object source, String filter) {
		if (source == null || filter == null || filter.trim().length() <= 0) {
			return source;
		}
		if (source instanceof List) {
			List list = (List) source;
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i) instanceof Map) {
					Map<String, Object> target = new LinkedHashMap<String, Object>();
					copyFields((Map<String, Object>)list.get(i), target, filter);
					list.set(i, target);
				}
			}
		}
		if (source instanceof Map) {
			Map<String, Object> target = new LinkedHashMap<String, Object>();
			copyFields((Map<String, Object>)source, target, filter);
			return target;
		}
		return source;
	}

	protected static void copyFields(Map<String, Object> from, Map<String, Object> to, String filter) {
		String[] fields = filter.split(",");
		for (String field : fields) {
			copyValues(from, to, field);
		}
	}

	protected static void copyValues(Map<String, Object> from, Map<String, Object> to, String field) {
		String[] names = field.split("\\.");
		Map<String, Object> mapA = from;
		Map<String, Object> mapB = to;
		for (int i = 0; i < names.length; i++) {
			String name = names[i];
			if (name == null || name.trim().length() <= 0)
				continue;
			if (!mapA.keySet().contains(name))
				break;
			Object valA = mapA.get(name);
			Object valB = mapB.get(name);

			if (i == names.length - 1) {
				if (valB == null || valA == null) {
					mapB.put(name, valA);
				} else {
					if (valB instanceof Map) {
						mapA = (Map<String, Object>) valA;
						mapB = (Map<String, Object>) valB;
						for (String key : mapA.keySet()) {
							mapB.put(key, mapA.get(key));
						}
					} else {
						mapB.put(name, valA);
					}
				}
			} else {
				if (valA != null && valA instanceof List) {
					List listA = (List) valA;
					if (listA.size() > 0 && listA.get(0) instanceof Map) {

						StringBuffer sb = new StringBuffer();
						for (int j = i + 1; j < names.length; j++) {
							sb.append(sb.length() > 0 ? "." : "");
							sb.append(names[j]);
						}

						List listB = null;
						if(valB==null){
							listB = new LinkedList();
							for(int j=0; j<listA.size(); j++){
								listB.add(new LinkedHashMap<String,Object>());
							}
						}else{
							listB = (List)valB;
						}
						
						for(int j=0; j<listA.size(); j++){
							Map<String, Object> tmpA = (Map<String, Object>) listA.get(j);
							Map<String, Object> tmpB = (Map<String, Object>) listB.get(j);
							copyValues(tmpA, tmpB, sb.toString());
						}

						mapB.put(name, listB);
						break;
					} else {
						mapB.put(name, valA);
					}
				} else if (valA != null && valA instanceof Map) {
					mapA = (Map<String, Object>) valA;
					if (valB == null) {
						valB = new HashMap<String, Object>();
						mapB.put(name, valB);
					}
					mapB = (Map<String, Object>) valB;
				} else {
					mapB.put(name, valA);
				}
			}
		}
	}

}

package com.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Sorter {

	public static Map asc(Map map) {
		return new Sorter().sortValues(map, false);
	}

	public static Map desc(Map map) {
		return new Sorter().sortValues(map, true);
	}

	public static Map ascKeys(Map map) {
		return new Sorter().sortKeys(map, false);
	}

	public static Map descKeys(Map map) {
		return new Sorter().sortKeys(map, true);
	}
	
	private Map sortValues(Map map, boolean reverse) {
		Map sortMap = new LinkedHashMap();

		List list = new ArrayList();
		Iterator it = map.keySet().iterator();
		while (it.hasNext()) {
			Object o = it.next();
			Object v = map.get(o);
			if (v == null) {
				list.add(new Item(o, null));
				continue;
			}
			if (v instanceof Comparable) {
				list.add(new Item(o, (Comparable) v));
				continue;
			}
			return map;
		}

		Collections.sort(list);
		if (reverse) {
			Collections.reverse(list);
		}

		for (int i = 0; i < list.size(); i++) {
			Item item = (Item) list.get(i);
			sortMap.put(item.o, item.v);
		}
		return sortMap;
	}

	private class Item implements Comparable {
		private Object o;
		private Comparable v;

		public Item(Object o, Comparable v) {
			this.o = o;
			this.v = v;
		}

		public int compareTo(Object t) {
			if (t == null || !(t instanceof Item))
				return 1;
			Item item = (Item) t;
			if (v == null && item.v == null)
				return 0;
			if (v != null && item.v == null)
				return 1;
			if (v == null && item.v != null)
				return -1;
			
			int rs = v.compareTo(item.v);
			
			if(rs==0 && o instanceof Comparable){
				if (o == null && item.o == null)
					return 0;
				if (o != null && item.o == null)
					return 1;
				if (o == null && item.o != null)
					return -1;
				if(item.o instanceof Comparable){
					return ((Comparable)o).compareTo(item.o);
				}
			}
			
			return v.compareTo(item.v);
		}
	}
	
	private Map sortKeys(Map map, boolean reverse) {
		Map sortMap = new LinkedHashMap();

		List list = new ArrayList();
		Iterator it = map.keySet().iterator();
		while (it.hasNext()) {
			Object o = it.next();
			Object v = map.get(o);
			if (o == null) {
				list.add(new Key(null, v));
				continue;
			}
			if (o instanceof Comparable) {
				list.add(new Key((Comparable) o, v));
				continue;
			}
			return map;
		}

		Collections.sort(list);
		if (reverse) {
			Collections.reverse(list);
		}

		for (int i = 0; i < list.size(); i++) {
			Key key = (Key) list.get(i);
			sortMap.put(key.o, key.v);
		}
		return sortMap;
	}
	
	private class Key implements Comparable {
		private Comparable o;
		private Object v;

		public Key(Comparable o, Object v) {
			this.o = o;
			this.v = v;
		}

		public int compareTo(Object t) {
			if (t == null || !(t instanceof Key))
				return 1;
			Key key = (Key) t;
			if (o == null && key.o == null)
				return 0;
			if (o != null && key.o == null)
				return 1;
			if (o == null && key.o != null)
				return -1;
			
			int rs = o.compareTo(key.o);
			
			if(rs==0 && v instanceof Comparable){
				if (v == null && key.v == null)
					return 0;
				if (v != null && key.v == null)
					return 1;
				if (v == null && key.v != null)
					return -1;
				if(key.v instanceof Comparable){
					return ((Comparable)v).compareTo(key.v);
				}
			}
			
			return o.compareTo(key.o);
		}
	}

}

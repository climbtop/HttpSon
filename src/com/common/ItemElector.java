package com.common;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class ItemElector<T> {

	private double minRate = 0;
	private Hashtable<Integer, ItemBean> electorMap;

	public ItemElector(int fullCount, double minRate, List<T> electors) {
		this.minRate = minRate;
		this.electorMap = new Hashtable<Integer, ItemBean>();
		for (int i = 0; i < electors.size(); i++) {
			T item = electors.get(i);
			ItemBean et = new ItemBean(i, fullCount, item);
			electorMap.put(new Integer(i), et);
		}
	}

	public synchronized T elect(Integer[] rtn) {
		List<Integer> list = electorMap==null?
				new ArrayList<Integer>():new ArrayList<Integer>(electorMap.keySet());
		if (list.size() <= 0) {
			if (rtn != null && rtn.length > 0) {
				rtn[0] = null;
			}
			return null;
		}
		Integer key = new Random().nextInt(list.size());
		ItemBean et = electorMap.get(key);
		if (et == null) {
			if (rtn != null && rtn.length > 0) {
				rtn[0] = null;
			}
			return null;
		}
		if (rtn != null && rtn.length > 0) {
			rtn[0] = key;
		}
		return et.getItem();
	}

	public synchronized void collect(Integer key, Object value) {
		if (key == null || electorMap==null) {
			return;
		}
		ItemBean et = electorMap.get(key);
		if (et == null) {
			return;
		}
		et.collect(value);
		if (et.isFull() && et.getRate() < minRate) {
			electorMap.remove(key);
		}
	}
	
	public synchronized void collect(Integer[] rtn, Object value) {
		collect(rtn.length>0?rtn[0]:null, value);
	}
	
	public synchronized int aliveCount() {
		if(electorMap==null) return 0;
		return electorMap.size();
	}
	
	public void clear() {
		if(electorMap!=null){
			electorMap.clear();
		}
	}

	private class ItemBean {
		private int id;
		private int full;
		private T item;
		private LinkedList<Boolean> queue;

		public ItemBean(int id, int full, T item) {
			this.id = id;
			this.item = item;
			this.queue = new LinkedList<Boolean>();
			this.full = full;
		}

		public void collect(Object value) {
			if (value != null && value instanceof Boolean) {
				queue.addFirst((Boolean) value);
				if (queue.size() > full) {
					queue.removeLast();
				}
			}
		}
		
		public boolean isFull() {
			return queue.size() >= full;
		}

		public int getId() {
			return id;
		}

		public T getItem() {
			return item;
		}

		public double getRate() {
			int success = 0;
			for (Boolean b : queue) {
				if (b)
					success++;
			}
			if (queue.size() == 0)
				return 1;
			return (1.0 * success) / queue.size();
		}
	}

}

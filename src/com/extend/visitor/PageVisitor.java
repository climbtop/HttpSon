package com.extend.visitor;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.extend.HttpGet;
import com.httpclient.HttpHelper;
import com.httpclient.HttpSimple;

abstract public class PageVisitor {

			private Set sets = new HashSet();
			private Set newed = new HashSet();
			private Set visited = new HashSet();
			private LinkedList errors = new LinkedList();
			protected int waittime = 30*1000;
			private HttpHelper hs = null;
			
			public PageVisitor(){
				hs = new HttpSimple();
				this.errors.clear();
			}
			
			public PageVisitor(HttpHelper hs, int waittime){
				this.hs = hs;
				this.waittime = waittime;
				this.errors.clear();
			}
			
			public void visit(String s){
				visited.add(s);
				try{
					hs = new HttpSimple();
					HttpGet get = new HttpGet(s);
					get.setTimeout(waittime);
					get.setUsedPool(true);
					hs.visitURI(get);
					String content = get.getContent(null);
					this.parseContent(content,sets);
					this.parseNext(content,newed);
				}catch(Throwable e){
					errors.addLast(s+"\r\n"+e+"");
				}
				this.next();
			}
			
			abstract public void parseNext(String content,Set newed);
			
			abstract public void parseContent(String content,Set sets);

			private void next(){
				String s = null;
				Iterator t = newed.iterator();
				while(t.hasNext()){
					s = (String)t.next();
					break;
				}
				if(s!=null) {
					newed.remove(s);
					visit(s);
				}
			}
			
			public List getSets(){
				return new LinkedList(sets);
			}
			
			public List getVisited(){
				return new LinkedList(visited);
			}
			
			public List getErrors(){
				return new LinkedList(errors);
			}
			
			public void print(){
				for(Object o : visited){
					System.out.println(o);
				}
				System.out.println("visited:"+visited.size());
				System.out.println();
				for(Object o : sets){
					System.out.println(o);
				}
				System.out.println("sets:"+sets.size());
			}
}

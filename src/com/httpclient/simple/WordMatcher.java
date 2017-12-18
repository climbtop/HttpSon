package com.httpclient.simple;

//words matcher
public class WordMatcher {
	
	private String words;
	private boolean hasfind;
	private   int l;
	protected int p;
	
	public WordMatcher(String words){
		this.reset();
		this.words = words;
	}
	
	public void reset(){
		this.hasfind = false;
		this.l = -1;
		this.p = 0;
	}
	
	public boolean check(int c){
		if(hasfind)return false;
		this.setLast(c);
		return this.push(c);
	}
	
	public int getLast(){
		return l;
	}
	
	private void setLast(int c){
		if(c==-1 || c==' ' || c=='\t' 
		   || c=='\r' || c=='\n')
		return;
		this.l = c;
	}

	protected boolean push(int c) {
		if(hasfind || words==null || words.length()<=0 ) 
			return false;
		
		if(c==words.charAt(p)){
			p ++;
			if(p==words.length()){
				hasfind = true;
				p = 0;
				return true;
			}
		}else{
			p = 0;
		}
		
		return false;
	}

	public boolean hasfind(){
		return hasfind;
	}
}
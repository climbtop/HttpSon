package com.common;

import java.util.LinkedList;
import java.util.List;

import com.extend.Httper;

public class TableFilter {
	private String content;
	private String startTag, endTag;
	private String startWord, endWord;

	public TableFilter(String content) {
		this.content = content;
	}
	
	public void forInit(String startTag, String endTag,
			String startWord, String endWord){
		this.startTag = startTag;
		this.endTag = endTag;
		this.startWord = startWord;
		this.endWord = endWord;
	}

	public List<String> forList() {
		List<String> list = new LinkedList<String>();
		if(startTag == null || endTag==null || startWord==null || endWord==null){
			return list;
		}
		String text = Httper.substring(startTag, endTag, content);
		do {
			String tr = Httper.substring(startWord, endWord, text);
			text = Httper.delstring(startWord, endWord, text);
			if (tr == null || tr.trim().length() <= 0)
				break;
			list.add(tr);
		} while (true);
		return list;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getStartTag() {
		return startTag;
	}

	public void setStartTag(String startTag) {
		this.startTag = startTag;
	}

	public String getEndTag() {
		return endTag;
	}

	public void setEndTag(String endTag) {
		this.endTag = endTag;
	}

	public String getStartWord() {
		return startWord;
	}

	public void setStartWord(String startWord) {
		this.startWord = startWord;
	}

	public String getEndWord() {
		return endWord;
	}

	public void setEndWord(String endWord) {
		this.endWord = endWord;
	}
}

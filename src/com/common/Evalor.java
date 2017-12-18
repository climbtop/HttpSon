package com.common;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Evalor {
	
	private Map<String,Object> objects = new HashMap<String,Object>();
	private Map<String,Object> globals = new HashMap<String,Object>();
	private Map<String,Object> variables = new HashMap<String,Object>();
	private Object defaultObject;
	protected boolean debug = false;
	private   char funcMarked = '`';
	
	public Evalor(){
		setContext("this",this);
		setDefault(Math.class);
	}
	
	public void setDefault(Object object){
		defaultObject = object;
	}
	public void setContext(String name, Object object){
		objects.put(name, object);
	}
	public void setGlobals(String name, Object object){
		globals.put(name, object);
	}
	protected void setVariable(String name, Object object){
		variables.put(name, object);
	}
	public void setContext(Map<String,Object> objects){
		this.objects.putAll(objects);
	}
	
	/**
	 * 变量填充值
	 */
	protected String fillValues(String s){
		Iterator it = globals.keySet().iterator();
		while(it.hasNext()){
			String key = String.valueOf(it.next());
			s = s.replace(key, stringValueOf(globals.get(key)));
		}
		return s;
	}
	
	/**
	 * 执行表达式
	 */
	public Object eval(String s){
		variables.clear();
		return evalMoreFunc(s);
	}
	
	/**
	 * 执行多个函数表达式
	 */
	protected Object evalMoreFunc(String s){
		s = format(s);
		while(s.indexOf(funcMarked)!=-1 && s.indexOf(funcMarked)>=0 ){
			int mid = s.indexOf(funcMarked);
			int start=-1;
			int end=-1;
			int temp=-1;
			
			int count = 0;
			for(int i=mid+1; i<s.length(); i++){
				char c = s.charAt(i);
				if(c=='('){
					count ++;
				}
				if(c==')'){
					count --;
				}
				if(c==')' && count==0){
					end = i;
					break;
				}
			}
			
			for(int j=mid-1; j>=0; j--){
				char c = s.charAt(j);
				if(c == '.'){
					temp = j;
					break;
				}
			}
			
			int j;
			boolean block = false;
			for(j=temp-1; j>=0; j--){
				char x = s.charAt(j);
				if(x==' '||x=='\t'||x=='\r'||x=='\n'){
					if(!block){
						start = j;
						continue;
					}
				}
				if(x>='0' && x<='9'){
					start = j;
					block = true;
					continue;
				}
				if(x>='a' && x<='z' || x>='A' && x<='Z' || x=='_' || x=='$'){
					start = j;
					block = true;
					continue;
				}
				break;
			}
			if(start<0 && j<=0){
				start = 0;
			}

			String startStr = start>=0?s.substring(0,start):"";
			String endStr = (end>=0&&end<=s.length()-1)?s.substring(end+1):"";
			String func = s.substring(start,end+1).trim();
			
			if((startStr+endStr).trim().length()<=0){
				if(s.indexOf(funcMarked)!=s.lastIndexOf(funcMarked)){
					start = func.indexOf('(');
					end   = func.lastIndexOf(')');
					
					startStr = func.substring(0,start+1);
					endStr = func.substring(end);
					func = func.substring(start+1,end);
					
					Object rs = evalMoreFunc(func);
					s = startStr.trim() +" "+ stringValueOf(rs) +" "+ endStr.trim();
				}else{
					return evalOneFunc(s);
				}
			}else{
				Object rs = evalMoreFunc(func);
				s = startStr.trim() +" "+ stringValueOf(rs) +" "+ endStr.trim();
			}
		}
		
		return evalOneFunc(s);
	}
	
	/**
	 * 执行单个函数表达式
	 */
	protected Object evalOneFunc(String s){
		s = format(s);
		Object value = evalAndOr(s);
		return value;
	}
	
	/**
	 * 执行(比较)表达式
	 * 引用执行(函数)表达式
	 * 引用执行(算术)表达式
	 */
	protected Object evalCompare(String s){
		s = s.trim();
		String[] symbols = {">=","<=","==","!=","<>",">","<"};
		String symbol = null;
		for(int i=0; i<symbols.length;i++){
			if(s.indexOf(symbols[i])>=0){
				symbol = symbols[i];
				break;
			}
		}
		if(symbol!=null){
			String s1 = s.substring(0,s.indexOf(symbol));
			String s2 = s.substring(s.indexOf(symbol)+symbol.length());
			Object v1 = null, v2 = null;
			try{ v1 = evalFuncFormula(s1); }catch(Throwable e){}
			try{ v2 = evalFuncFormula(s2); }catch(Throwable e){}
			
			String null1 = v1==null?s1:String.valueOf(v1);
			String null2 = v2==null?s2:String.valueOf(v2);
			if("null".equalsIgnoreCase(null1) || "null".equalsIgnoreCase(null2)){
				if("==".equals(symbol)){
					return new Boolean(null1.equalsIgnoreCase(null2));
				}
				if("!=".equals(symbol)||"<>".equals(symbol)){
					return new Boolean(!null1.equalsIgnoreCase(null2));
				}
			}
			
			try {
				if (v1 == null || v2 == null) {
					throw new Throwable();
				}
				Double d1 = Double.valueOf(String.valueOf(v1));
				Double d2 = Double.valueOf(String.valueOf(v2));
				if (">".equals(symbol)) {
					return new Boolean(d1.doubleValue() > d2.doubleValue());
				}
				if (">=".equals(symbol)) {
					return new Boolean(d1.doubleValue() >= d2.doubleValue());
				}
				if ("<".equals(symbol)) {
					return new Boolean(d1.doubleValue() < d2.doubleValue());
				}
				if ("<=".equals(symbol)) {
					return new Boolean(d1.doubleValue() <= d2.doubleValue());
				}
				if ("==".equals(symbol)) {
					return new Boolean(d1.doubleValue() == d2.doubleValue());
				}
				if ("!=".equals(symbol) || "<>".equals(symbol)) {
					return new Boolean(d1.doubleValue() != d2.doubleValue());
				}
				throw new Throwable();
			} catch (Throwable throwable1) {
				try {
					if (v1 == null || v2 == null) {
						throw throwable1;
					}
					if (v1 instanceof Comparable && v2 instanceof Comparable) {
						Comparable c1 = (Comparable) v1;
						Comparable c2 = (Comparable) v2;
						if (">".equals(symbol)) {
							return new Boolean(c1.compareTo(c2) > 0);
						}
						if (">=".equals(symbol)) {
							return new Boolean(c1.compareTo(c2) >= 0);
						}
						if ("<".equals(symbol)) {
							return new Boolean(c1.compareTo(c2) < 0);
						}
						if ("<=".equals(symbol)) {
							return new Boolean(c1.compareTo(c2) <= 0);
						}
						if ("==".equals(symbol)) {
							return new Boolean(c1.compareTo(c2) == 0);
						}
						if ("!=".equals(symbol) || "<>".equals(symbol)) {
							return new Boolean(c1.compareTo(c2) != 0);
						}
					}
					throw throwable1;
				} catch (Throwable throwable2) {
					if (">".equals(symbol)) {
						return new Boolean(null1.compareTo(null2) > 0);
					}
					if (">=".equals(symbol)) {
						return new Boolean(null1.compareTo(null2) >= 0);
					}
					if ("<".equals(symbol)) {
						return new Boolean(null1.compareTo(null2) < 0);
					}
					if ("<=".equals(symbol)) {
						return new Boolean(null1.compareTo(null2) <= 0);
					}
					if ("==".equals(symbol)) {
						return new Boolean(null1.compareTo(null2) == 0);
					}
					if ("!=".equals(symbol) || "<>".equals(symbol)) {
						return new Boolean(null1.compareTo(null2) != 0);
					}
					if (debug) {
						System.out.println(v1 + " " + symbol + " " + v2 + "-->" + "can not compare" + "\r\n" +
								           null1 + " " + symbol + " " + null2 + "-->" + "also not compare");
					}
				}
			}

		}
		return evalFuncFormula(s);
	}
	
	/**
	 * 自动判断(函数)(算术)表达式
	 */
	protected Object evalFuncFormula(String s){
		boolean func = false;
		if(s.indexOf(funcMarked)>=0 || s.indexOf(';')>=0){
			func = true;
		}
		if(!func){
			String t = fillValues(s);
			boolean NaN = true;
			for(int i=0; i<t.length(); i++){
				char c = t.charAt(i);
				if(c>='a' && c<='z' || c>='A' && c<='Z' || c=='_' || c=='$'){
					func = true;
					break;
				}
				if(c>='0' && c<='9'){
					NaN = false;
					continue;
				}
				if(c>='+' || c<='-' || c=='*' || c=='/'){
					continue;
				}
				if(c==' '||c=='\t'||c=='\r'||c=='\n'){
					continue;
				}
				if(c=='('||c==')' || c=='.'){
					continue;
				}
				func = true;
				break;
			}
			if(NaN){
				func = true;
			}
		}
		if(func){
			return evalFunc(s);
		}else{
			return evalFormula(s);
		}
	}
	
	/**
	 * 执行(函数)表达式
	 */
	protected Object evalFunc(String s){
		s = s.trim();
		if(s.endsWith(";")){
			s = s.substring(0,s.length()-1);
		}
		boolean single = true;
		if(s.startsWith("!")){
			s = s.substring(1);
			single = false;
		}

		Object object = defaultObject;
		Iterator it1 = variables.keySet().iterator();
		while(it1.hasNext()){
			String key = String.valueOf(it1.next());
			Object val = variables.get(key);
			if(s.startsWith(key+".")){
				s = s.substring((key+".").length());
				object = val;
				break;
			}
		}
		
		Iterator it2 = objects.keySet().iterator();
		while(it2.hasNext()){
			String key = String.valueOf(it2.next());
			Object val = objects.get(key);
			if(s.startsWith(key+".")){
				s = s.substring((key+".").length());
				object = val;
				break;
			}
		}
		
		String func = "";
		if(s.indexOf(funcMarked)>0){
			func = s.substring(0,s.indexOf(funcMarked));
			s = s.substring(s.indexOf(funcMarked)+1);
		}else{
			func = s;
			s = "";
		}
		s = fillValues(s);
		List args = asArgsList(s);

		Object callRulest = propertyCall(object, func, args);
		if(callRulest!=null && !single && (callRulest instanceof Boolean)){
			return ((Boolean)callRulest).booleanValue()?
					new Boolean(false):new Boolean(true);
		}
		return callRulest;
	}
	
	
	/**
	 * 属性方式调用
	 */
	protected Object propertyCall(Object object, String propName, List args){
		if(object !=null && object instanceof Map){
			Map map = (Map)object;
			return map.get(propName);
		}
		if(object !=null && object instanceof Iterable){
			Iterator it = ((Iterable)object).iterator();
			int i = 0;
			while(it.hasNext()){
				Object obj = it.next();
				if(String.valueOf(i).equals(propName)){
					return obj;
				}
				i ++;
			}
			if(debug){
				System.out.println("not found list index:"+propName+"---->"+null);
			}
		}
		if(object !=null && object instanceof Object[]){
			Object[] it = (Object[])object;
			for(int i=0; i<it.length; i++){
				Object obj = it[i];
				if(String.valueOf(i).equals(propName)){
					return obj;
				}
			}
			if(debug){
				System.out.println("not found array index:"+propName+"---->"+null);
			}
		}
		
		if(propName==null || propName.length()<=0){
			return functionCall(object, propName, args);
		}
		
		boolean isProperty = false;
		String newFuncName = "get"+String.valueOf(propName.charAt(0)).toUpperCase()+
        					 (propName.length()>0?propName.substring(1):"");
		if(object !=null ){
			Method[] ms = getObjectClass(object).getMethods();
			for(int i=0; i<ms.length; i++){
				Method m = ms[i];
				String methodName = m.getName();
				if(methodName.equals(newFuncName)){
					Class[] ts = m.getParameterTypes();
					ts = ts==null?new Class[0]:ts;
					if(ts.length==0 && args.size()==0){
						isProperty = true;
						break;
					}
				}
			}
		}
		if(isProperty){
			return functionCall(object, newFuncName, args);
		}else{
			return functionCall(object, propName, args);
		}
	}
	
	/**
	 * 返回对象Class
	 */
	protected Class getObjectClass(Object object){
		Class cls = null;
		if(object != null){
			if(object instanceof Class){
				cls = (Class)object;
			}else{
				cls = object.getClass();
			}
		}
		return cls;
	}
	
	/**
	 * 函数方式调用
	 */
	protected Object functionCall(Object object, String funcName, List args){
		Method[] ms = getObjectClass(object).getMethods();
		for(int i=0; i<ms.length; i++){
			Method m = ms[i];
			if(!m.getName().equals(funcName)){
				continue;
			}
			Class[] ts = m.getParameterTypes();
			ts = ts==null?new Class[0]:ts;
			if(args.size() == ts.length){
				Object[] parms = new Object[args.size()];
				for(int j=0; j<args.size(); j++){
					try{
						String v = stringValueOf(args.get(j));
						Class t = ts[j];
						
						if("int".equals(t.getName())){
							v = evalFormula(v);
							parms[j] = new Integer(Double.valueOf(v).intValue());
						}else if("long".equals(t.getName())){
							v = evalFormula(v);
							parms[j] = new Long(Double.valueOf(v).longValue());
						}else if("float".equals(t.getName())){
							v = evalFormula(v);
							parms[j] = new Float(Double.valueOf(v).floatValue());
						}else if("double".equals(t.getName())){
							v = evalFormula(v);
							parms[j] = Double.valueOf(v);
						}else if("java.lang.String".equals(t.getName())){
							parms[j] = String.valueOf(v);
						}else{
							v = evalFormula(v);
							Object o = newInstance(t, v);
							parms[j] = o;
						}
					} catch (Exception e) {
						if(debug){
							e.printStackTrace();
						}
					}
				}
				try {
					Object o = null;
					if(object instanceof Class){
						o = m.invoke(null, parms);
					}else{
						o = m.invoke(object, parms);
					}
					return o;
				} catch (Exception e) {
					if(debug){
						e.printStackTrace();
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * 数据类型实例化
	 */
	protected Object newInstance(Class t, String v){
		Object o = null;
		if(o==null){
			try{
				Constructor cls = t.getConstructor();
				if(cls!=null){
					o = cls.newInstance();
					Method x = o.getClass().getMethod("valueOf", 
							new Class[]{String.class});
					if(x!=null){
						o = x.invoke(o, new Object[]{v});
					}
				}
			}catch(Exception e){
				o = null;
			}
		}
		if(o==null){
			try{
				Method x = t.getMethod("valueOf", 
						new Class[]{String.class});
				if(x!=null){
					o = x.invoke(null, new Object[]{v});
				}
			}catch(Exception e){
				o = null;
			}
		}
		if(o==null){
			try{
				Constructor cls = t.getConstructor(String.class);
				if(cls!=null){
					o = cls.newInstance(v);
				}
			}catch(Exception e){
				o = null;
			}
		}
		return o;
	}
	
	/**
	 * 执行(与或)表达式
	 */
	protected Object evalAndOr(String s){
		s = s.trim();
		List list = parseAndOr(s);
		return calcAndOr(list);
	}
	
	/**
	 * 用'`'标记函数
	 */
	private String format(String s){
		s = s.trim();
		while(s.endsWith(";")){
			s = s.substring(0,s.length()-1);
		}
		s = formatCommonKeyword(s);
		s = formatFuncMarked(s);
		s = convertToFunc(s);
		return s;
	}
	private String formatCommonKeyword(String s){
		s = s.replaceAll("\\s+(?i)And(?-i)\\s+", " && ");
		s = s.replaceAll("\\s+(?i)Or(?-i)\\s+", " || ");
		return s;
	}
	private String formatFuncMarked(String s){
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<s.length(); i++){
			char c = s.charAt(i);
			if(c == '('){
				boolean mark = false;
				for(int j=i-1; j>=0; j--){
					char x = s.charAt(j);
					if(x==' '||x=='\t'||x=='\r'||x=='\n'){
						continue;
					}
					if(x>='0' && x<='9'){
						continue;
					}
					if(x>='a' && x<='z' || x>='A' && x<='Z' || x=='_' || x=='$'){
						mark = true;
						break;
					}
					mark = false;
					break;
				}
				if(mark){
					sb.append(funcMarked);
				}
			}
			sb.append(c);
		}
		return sb.toString();
	}
	private String convertToFunc(String s){
		List dexs = new ArrayList();
		for(int i=0; i<s.length(); i++){
			char c = s.charAt(i);
			boolean isObj = false, block = false;
			if(c == '.'){
				for(int j=i-1; j>=0; j--){
					char x = s.charAt(j);
					if(x==' '||x=='\t'||x=='\r'||x=='\n'){
						if(!block){
						   continue;
						}
					}
					if(x>='0' && x<='9'){
						block = true;
						continue;
					}
					if(x>='a' && x<='z' || x>='A' && x<='Z' || x=='_' || x=='$'){
						isObj = true;
						break;
					}
					isObj = false;
					break;
				}
			}
			int dex = -1;
			if(isObj){
				block = false;
				int j;
				for(j=i+1; j<s.length(); j++){
					char x = s.charAt(j);
					if(x==' '||x=='\t'||x=='\r'||x=='\n'){
						if(!block){
							continue;
						}
					}
					if(x>='0' && x<='9'){
						block = true;
						continue;
					}
					if(x>='a' && x<='z' || x>='A' && x<='Z' || x=='_' || x=='$' || x==funcMarked){
						block = true;
						continue;
					}
					if(x == '('){
						dex = -1;
						break;
					}
					dex = j-1;
					break;
				}
				if(dex<0 && j==s.length()){
					dex = s.length()-1;
				}
			}
			if(dex>=0){
				dexs.add(dex);
			}
		}

		StringBuffer sb = new StringBuffer();
		for(int i=0; i<s.length(); i++){
			char c = s.charAt(i);
			sb.append(c);
			for(int j=0; j<dexs.size(); j++){
				int k = ((Integer)dexs.get(j)).intValue();
				if(i == k){
					sb.append(funcMarked);
					sb.append('(');
					sb.append(')');
				}
			}
		}
		return sb.toString();
	}
	
	/**
	 * 解析(与或)表达式
	 */
	private List parseAndOr(String s){
		s = s.replaceAll("\\&\\&", "&");
		s = s.replaceAll("\\|\\|", "|");
		
		List list = new ArrayList();
		char last = ' ';
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<s.length(); i++){
			char c = s.charAt(i);
			if(c==' '||c=='\t'||c=='\r'||c=='\n'){
				continue;
			}
			if(c==')'){
				if(sb.length()>0){
					list.add(sb.toString());
					sb.delete(0, sb.length());
				}
				list.add(String.valueOf(c));
				continue;
			}
			if(c=='('){
				if(last==funcMarked){
					sb.append(c);
					int count = 1;
					while(true){
						c=s.charAt(++i);
						if(c=='(')count++;
						if(c==')')count--;
						if(count<=0)break;
						sb.append(c);
					}
				}else{
					if(sb.length()>0){
						list.add(sb.toString());
						sb.delete(0, sb.length());
					}
					list.add(String.valueOf(c));
					continue;
				}
			}
			if(c=='&'){
				if(sb.length()>0){
					list.add(sb.toString());
					sb.delete(0, sb.length());
				}
				list.add(String.valueOf(c));
				continue;
			}
			if(c=='|'){
				if(sb.length()>0){
					list.add(sb.toString());
					sb.delete(0, sb.length());
				}
				list.add(String.valueOf(c));
				continue;
			}
			
			sb.append(c);
			last = c;
		}
		if(sb.length()>0){
			list.add(sb.toString());
			sb.delete(0, sb.length());
		}
		
		//Union Not AndOr Express
		int length = -1;
		do{
			length = list.size();
			for(int i=0; i<list.size(); i++){
				String x = String.valueOf(list.get(i));
				if(x.indexOf('&')<0 && x.indexOf('|')<0){
					if(i-1>=0 && i+1<list.size()){
						String m = String.valueOf(list.get(i-1));
						String n = String.valueOf(list.get(i+1));
						if(m.equals("(") && n.equals(")")){
							list.set(i, m + x + n);
							list.remove(i+1);
							list.remove(i-1);
							break;
						}
					}
					if(i-1>=0){
						String m = String.valueOf(list.get(i-1));
						if(m.indexOf('&')<0 && m.indexOf('|')<0){
							if(!m.equals("(")&&!m.equals(")") && !x.equals("(")&&!x.equals(")")){
								list.set(i, m + x);
								list.remove(i-1);
								break;
							}
						}
					}
					if(i+1<list.size()){
						String n = String.valueOf(list.get(i+1));
						if(n.indexOf('&')<0 && n.indexOf('|')<0){
							if(!n.equals("(")&&!n.equals(")") && !x.equals("(")&&!x.equals(")")){
								list.set(i, x + n);
								list.remove(i+1);
								break;
							}
						}
					}
				}
			}
		}while(length != list.size());
		
		return list;
	}
	
	/**
	 * 计算(与或)表达式
	 * 引用执行(比较)表达式
	 */
	private Object calcAndOr(List list){
		for(int i=0; i<list.size(); i++){
			String s = String.valueOf(list.get(i));
			s = s.trim();
			if("true".equals(s) || "false".equals(s) ||
				"(".equals(s)||")".equals(s)||"&".equals(s)||"|".equals(s)){
				continue;
			}
			while(s.startsWith("(") && s.endsWith("")){
				s = s.substring(1, s.length()-1);
			}
			Object value = evalCompare(s);
			if(value!=null && value instanceof Boolean){
				list.set(i, String.valueOf(value));
			}else{
				list.set(i, value);
			}
		}
		
		for(int i=0; i<list.size(); i++){
			String s = String.valueOf(list.get(i));
			if(")".equals(s)){
				LinkedList sub = new LinkedList();
				int j;
				for(j=i-1; j>=0; j--){
					String t = String.valueOf(list.get(j));
					if("(".equals(t)){
						break;
					}
					sub.add(0, t);
				}
				for(int k=i; k>=j; k--){
					list.remove(k);
				}
				Object value = calcAndOr(sub);
				if(value!=null && value instanceof Boolean){
					list.add(j, String.valueOf(value));
					i = -1;
				}else if(value!=null){
					list.add(j, value);
					i = -1;
				}else{
					if(debug){
						System.out.println(toString(sub)+"---->"+value);
					}
					return null;
				}
			}
		}
		
		for(int i=0; i<list.size(); i++){
			String s = String.valueOf(list.get(i));
			if("&".equals(s)){
				if(i-1>=0 && i+1<list.size()){
					Boolean a = Boolean.valueOf(list.get(i-1)+"");
					Boolean b = Boolean.valueOf(list.get(i+1)+"");
					Boolean value = new Boolean(a.booleanValue() && b.booleanValue());
					list.remove(i+1);
					list.remove(i);
					list.remove(i-1);
					list.add(i-1, String.valueOf(value));
					i = -1;
				}else{
					if(debug){
						String a = (i-1>=0?list.get(i-1)+"":"");
						String b = (i+1<list.size()?list.get(i+1)+"":"");
						System.out.println(s+"----->"+(i-1)+":"+a+", "+i+":"+s+", "+(i+1)+":"+b);
					}
					return null;
				}
			}
		}
		
		for(int i=0; i<list.size(); i++){
			String s = String.valueOf(list.get(i));
			if("|".equals(s)){
				if(i-1>=0 && i+1<list.size()){
					Boolean a = Boolean.valueOf(list.get(i-1)+"");
					Boolean b = Boolean.valueOf(list.get(i+1)+"");
					Boolean value = new Boolean(a.booleanValue() || b.booleanValue());
					list.remove(i+1);
					list.remove(i);
					list.remove(i-1);
					list.add(i-1, String.valueOf(value));
					i = -1;
				}else{
					if(debug){
						String a = (i-1>=0?list.get(i-1)+"":"");
						String b = (i+1<list.size()?list.get(i+1)+"":"");
						System.out.println(s+"------>"+(i-1)+":"+a+", "+i+":"+s+", "+(i+1)+":"+b);
					}
					return null;
				}
			}
		}
		
		if(list!=null && list.size()==1){
			Object value = list.get(0);
			return value;
		}else{
			if(debug){
				System.out.println(toString(list)+"------->"+"lize() "+list.size()+" not equal one");
			}
			return null;
		}
	}
	
	/**
	 * 执行(算术)表达式
	 */
	protected String evalFormula(String s){
		s = s.trim();
		List list = parseFormula(s);
		return calcFormula(list);
	}
	
	/**
	 * 解析(算术)表达式
	 */
	private List parseFormula(String s){
		List list = new ArrayList();
		StringBuffer sb = new StringBuffer();
		if(s.startsWith("-")){
			s = "0"+s;
		}
		for(int i=0; i<s.length(); i++){
			char c = s.charAt(i);
			if(c==' '||c=='\t'||c=='\r'||c=='\n'){
				continue;
			}
			if(c=='+' || c=='-' || c=='*' || c=='/'){
				if(sb.length()>0){
					list.add(sb.toString());
					sb.delete(0, sb.length());
				}
				list.add(String.valueOf(c));
				continue;
			}
			if(c=='(' || c==')'){
				if(sb.length()>0){
					list.add(sb.toString());
					sb.delete(0, sb.length());
				}
				list.add(String.valueOf(c));
				continue;
			}
			sb.append(c);
		}
		if(sb.length()>0){
			list.add(sb.toString());
			sb.delete(0, sb.length());
		}
		return list;
	}
	
	/**
	 * 计算(算术)表达式
	 */
	private String calcFormula(List list){
		for(int i=0; i<list.size(); i++){
			String s = String.valueOf(list.get(i));
			s = s.trim();
			if("+".equals(s)||"-".equals(s)||"*".equals(s)||"/".equals(s)||
			   "(".equals(s)||")".equals(s)){
				continue;
			}
			String o = fillValues(s);
			if(s.indexOf(funcMarked)>=0){
				o = String.valueOf(evalFunc(s));
			}
			list.set(i, o);
		}
		
		for(int i=0; i<list.size(); i++){
			String s = String.valueOf(list.get(i));
			if(")".equals(s)){
				LinkedList sub = new LinkedList();
				int j;
				for(j=i-1; j>=0; j--){
					String t = String.valueOf(list.get(j));
					if("(".equals(t)){
						break;
					}
					sub.add(0, t);
				}
				for(int k=i; k>=j; k--){
					list.remove(k);
				}
				String value = calcFormula(sub);
				if(value!=null){
					list.add(j, String.valueOf(value));
					i = -1;
				}else{
					if(debug){
						System.out.println(toString(sub)+"-------->"+value);
					}
					return null;
				}
			}
		}
		
		for(int i=0; i<list.size(); i++){
			String s = String.valueOf(list.get(i));
			if("*".equals(s)||"/".equals(s)){
				if(i-1>=0 && i+1<list.size()){
					Double a = Double.valueOf(list.get(i-1)+"");
					Double b = Double.valueOf(list.get(i+1)+"");
					Double c = null;
					if("*".equals(s)){
						c = new Double(a.doubleValue() * b.doubleValue());
					}
					if("/".equals(s)){
						c = new Double(a.doubleValue() / b.doubleValue());
					}
					list.remove(i+1);
					list.remove(i);
					list.remove(i-1);
					list.add(i-1, String.valueOf(c));
					i = -1;
				}else{
					if(debug){
						String a = (i-1>=0?list.get(i-1)+"":"");
						String b = (i+1<list.size()?list.get(i+1)+"":"");
						System.out.println(s+"--------->"+(i-1)+":"+a+", "+i+":"+s+", "+(i+1)+":"+b);
					}
					return null;
				}
			}
		}
		
		for(int i=0; i<list.size(); i++){
			String s = String.valueOf(list.get(i));
			if("+".equals(s)||"-".equals(s)){
				if(i-1>=0 && i+1<list.size()){
					Double a = Double.valueOf(list.get(i-1)+"");
					Double b = Double.valueOf(list.get(i+1)+"");
					Double c = null;
					if("+".equals(s)){
						c = new Double(a.doubleValue() + b.doubleValue());
					}
					if("-".equals(s)){
						c = new Double(a.doubleValue() - b.doubleValue());
					}
					list.remove(i+1);
					list.remove(i);
					list.remove(i-1);
					list.add(i-1, String.valueOf(c));
					i = -1;
				}else{
					if(debug){
						String a = (i-1>=0?list.get(i-1)+"":"");
						String b = (i+1<list.size()?list.get(i+1)+"":"");
						System.out.println(s+"---------->"+(i-1)+":"+a+", "+i+":"+s+", "+(i+1)+":"+b);
					}
					return null;
				}
			}
		}
		
		if(list!=null && list.size()==1){
			String value = String.valueOf(list.get(0));
			return value;
		}else{
			if(debug){
				System.out.println(toString(list)+"----------->"+"lize() "+list.size()+" not equal one");
			}
			return null;
		}
	}
	
	/**
	 * 解析(函数)参数列表
	 */
	protected List asArgsList(String s){
		if(s.startsWith("(")){
			s = s.substring(1);
		}
		if(s.endsWith(")")){
			s = s.substring(0,s.length()-1);
		}
		List args = Arrays.asList(s.split(","));
		List list = new ArrayList();
		for(int i=0; i<args.size(); i++){
			Object o = args.get(i);
			if(o==null){
				continue;
			}
			String t = String.valueOf(o);
			if(t.trim().length()<=0){
				continue;
			}
			list.add(t.trim());
		}
		return list;
	}
	protected Object[] asArgsArray(String s){
		List list = asArgsList(s);
		Object[] args = new Object[list.size()];
		for(int i=0; i<list.size(); i++){
			args[i] = list.get(i);
		}
		return args;
	}
	
	/**
	 * 表达式输出字符串
	 */
	protected String toString(List list){
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<list.size(); i++){
			sb.append(" "+list.get(i)+" ");
		}
		return sb.toString();
	}
	
	/**
	 * 随机字符串
	 */
	public static String genRandomKey(int length) {
		String str = "abcdefghijklmnopqrstuvwxyz";
		Random random = new Random();
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < length; i++) {
			int num = random.nextInt(str.length());
			buf.append(str.charAt(num));
		}
		return buf.toString();
	}
	
	/**
	 * 载入临时变量
	 */
	protected String stringValueOf(Object object){
		if(object == null) return String.valueOf(object);
		String string = String.valueOf(object);
		if(string.indexOf('@')<0){
			return string;
		}
		String name = genRandomKey(10);
		setVariable(name, object);
		return name;
	}
	
}

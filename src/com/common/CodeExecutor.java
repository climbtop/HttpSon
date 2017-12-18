package com.common;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CodeExecutor {

	private Object defaultObject = this; 
	private Map<String,Object> valueMap = new HashMap<String,Object>();
	private Map<String,Object> classMap = new HashMap<String,Object>();
	
	public Object getDefaultObject() {
		return defaultObject;
	}
	public void setDefaultObject(Object defaultObject) {
		this.defaultObject = defaultObject;
	}
	public void addObject(Class cls){
		String name = cls.getSimpleName();
		if(name.endsWith(".class")){
			name = name.substring(0,name.lastIndexOf('.'));
		}
		if(name.indexOf(".")>0){
			name = name.substring(name.lastIndexOf('.')+1);
		}
		classMap.put(cls.getSimpleName(), cls);
	}
	public void addObject(String name, Object obj){
		classMap.put(name, obj);
	}
	public void removeObject(String name){
		classMap.remove(name);
	}
	public void addValue(String name, Object cls){
		valueMap.put(name, cls);
	}
	public void removeValue(String name){
		valueMap.remove(name);
	}
	
	public Object execute(List<String> codeLines){
		Object value = null;
		if(codeLines==null || codeLines.size()<=0){
			return value;
		}
		for(String codeLine : codeLines){
			try{
				codeLine = codeLine.trim();
				if(codeLine.startsWith("return")){
					codeLine = codeLine.substring("return".length());
					return execute(codeLine);
				}
				if(codeLine.startsWith("!return")){
					codeLine = codeLine.substring("!return".length());
					Object object = execute(codeLine);
					if(!isNull(object)){
						return execute(codeLine);
					}
				}
				value = execute(codeLine);
			}catch(Exception e){
				value = codeLine;
				e.printStackTrace();
			}
		}
		return value;
	}
	
	public Object executeMore(String codeLine) throws Exception{
		Object value = null;
		if(codeLine==null || codeLine.trim().length()<=0){
			return value;
		}
		codeLine = codeLine.replaceAll(";", ";\n");
		String[] codeLineArr = codeLine.split("\n");
		List<String> codeLines = new LinkedList<String>();
		for(String line : codeLineArr){
			line = line.trim();
			if(line.length()>0){
				codeLines.add(line);
			}
		}
		return execute(codeLines);
	}
	
	public Object execute(String codeLine) throws Exception{
		if(codeLine==null) return null;
		codeLine = codeLine.trim();
		if(codeLine.endsWith(";")){
			codeLine = codeLine.substring(0, codeLine.length()-1);
		}
		if(valueMap.get(codeLine)!=null){
			return valueMap.get(codeLine);
		}
		String value = null;
		if(codeLine.indexOf('=')>0){
			value = codeLine.substring(0,codeLine.indexOf("=")).trim();
			codeLine = codeLine.substring(codeLine.indexOf("=")+1).trim();
		}
		if(value!=null){
			Object result = execute(codeLine);
			valueMap.put(value, result);
			return result;
		}
		if(codeLine.matches(".*?\\[\\d+\\]$")){
			String parm = codeLine.substring(
					codeLine.lastIndexOf('[')+1, codeLine.lastIndexOf(']')).trim();
			codeLine = codeLine.substring(0,codeLine.lastIndexOf('['));
			Object result = execute(codeLine);
			result = invoke(result, new String[]{parm});
			return result;
		}
		
		Object defaultObject = getDefaultObject();
		if(codeLine.indexOf('.')>0){
			String name = codeLine.substring(0,codeLine.indexOf(".")).trim();
			codeLine = codeLine.substring(codeLine.indexOf(".")+1).trim();
			if(classMap.get(name)!=null){
				defaultObject = classMap.get(name);
			}else{
				if(valueMap.get(name)!=null){
					defaultObject = valueMap.get(name);
				}
			}
		}
		if(codeLine.indexOf('[')>0 && codeLine.endsWith("]")){
			String name = codeLine.substring(0,codeLine.indexOf("[")).trim();
			codeLine = codeLine.substring(codeLine.indexOf("[")).trim();
			if(classMap.get(name)!=null){
				defaultObject = classMap.get(name);
			}else{
				if(valueMap.get(name)!=null){
					defaultObject = valueMap.get(name);
				}
			}
		}
		if(defaultObject==null) return null;
		
		String methodName = "";
		if(codeLine.indexOf("(")>0){
			methodName = codeLine.substring(0, codeLine.indexOf("("));
			codeLine = codeLine.substring(codeLine.indexOf("(")).trim();
		}
		if(codeLine.startsWith("(") && codeLine.endsWith(")")){
			codeLine = codeLine.substring(1,codeLine.length()-1).trim();
		}
		if(codeLine.startsWith("[") && codeLine.endsWith("]")){
			methodName = "[]";
			codeLine = codeLine.substring(1,codeLine.length()-1).trim();
		}
		
		String[] parms = new String[0];
		if(codeLine.indexOf(",")>0){
			parms = codeLine.split(",");
		}else{
			if(codeLine.trim().length()>0){
				parms = new String[]{codeLine};
			}
		}

		if("[]".equals(methodName)){
			return invoke(defaultObject, parms);
		}
		if(defaultObject instanceof Class){
			Class cls = (Class)defaultObject;
			Method[] methods = cls.getMethods();
			return invoke(methods, null, methodName, parms);
		}else {
			Method[] methods = defaultObject.getClass().getMethods();
			return invoke(methods, defaultObject, methodName, parms);
		}
	}
	
	protected Object invoke(Object invoker, String[] parms) throws Exception{
		if(invoker == null) return null;
		if(invoker instanceof Object[]){
			Object[] arr = (Object[])invoker;
			Integer dex = Integer.valueOf(parms[0]);
			return arr[dex];
		}
		if(invoker instanceof Iterable){
			Iterable iter = (Iterable)invoker;
			Iterator it = iter.iterator();
			Integer cnt = 0;
			Integer dex = Integer.valueOf(parms[0]);
			while(it.hasNext()){
				Object obj = it.next();
				if(dex.intValue()==cnt.intValue()){
					return obj;
				}
				cnt ++;
			}
		}
		return null;
	}
	
	protected Object invoke(Method[] methods, Object invoker, String methodName, String[] parms) throws Exception{
		Method method = null;
		for(Method m : methods){
			if(m.getName().equals(methodName)){
				if(m.getParameterTypes().length==parms.length){
					method = m;
					break;
				}
			}
		}
		if(method==null) return null;
		Object[] objs = new Object[parms.length];
		for(int i=0; i<objs.length; i++){
			String parm = parms[i].trim();
			if(valueMap.get(parm)!=null){
				objs[i] = valueMap.get(parm);
				continue;
			}
			Class c = method.getParameterTypes()[i];
			c = getFormalClass(c);
			if(c == String.class){
				if(parm.startsWith("\"")&&parm.endsWith("\"")){
					parm = parm.substring(1,parm.length()-1);
				}
				objs[i] = parm;
				continue;
			}
			try{
				objs[i] = c.getMethod("valueOf", String.class).invoke(null, parm);
			}catch(Exception e){
				objs[i] = parm;
			}
		}
		return method.invoke(invoker, objs);
	}
	
	protected boolean isNull(Object object){
		if(object==null) return true;
		String val = String.valueOf(object);
		return "null".equalsIgnoreCase(val) || val.length()==0;
	}
	
	public Class getFormalClass(Class c){
		if(c == int.class){
			return Integer.class;
		}
		if(c == float.class){
			return Float.class;
		}
		if(c == double.class){
			return Double.class;
		}
		if(c == char.class){
			return Character.class;
		}
		if(c == boolean.class){
			return Boolean.class;
		}
		if(c == short.class){
			return Short.class;
		}
		if(c == long.class){
			return Long.class;
		}
		if(c == byte.class){
			return Byte.class;
		}
		if(c == int[].class){
			return Integer[].class;
		}
		if(c == float[].class){
			return Float[].class;
		}
		if(c == double[].class){
			return Double[].class;
		}
		if(c == char[].class){
			return Character[].class;
		}
		if(c == boolean[].class){
			return Boolean[].class;
		}
		if(c == short[].class){
			return Short[].class;
		}
		if(c == long[].class){
			return Long[].class;
		}
		if(c == byte[].class){
			return Byte[].class;
		}
		return c;
	}
}

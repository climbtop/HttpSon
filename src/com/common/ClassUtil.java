package com.common;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
/**
 * 根据对象名构造函数参数，创建对象.
 */
public class ClassUtil {

	public static Object newObject(Class<?> cls, Class<?>[] par, Object[] param) 
	throws Exception{
		if(cls==null) return null;
		Constructor<?> con = getConstructor(cls,par);
		return con.newInstance(param);
    }
	
	public static Object newObject(String className, Class<?>[] par, Object[] param) 
	throws Exception{
		if(className==null || param==null) return null;
		Class<?> cls = Class.forName(className);
		return newObject(cls,par,param);
	}
	
	public static Object newObject(Class<?> cls, Object[] param) 
		throws Exception{
		if(cls==null) return null;
		Class<?>[] par = new Class[param.length];
		for(int i=0; i<param.length; i++){
			Object fc = param[i];
			par[i] = fc.getClass();
		}
		Constructor<?> con = getConstructor(cls,par);
		return con.newInstance(param);
	}
	
	public static Object newObject(String className, Object[] param) 
	throws Exception{
		if(className==null || param==null) return null;
		Class<?> cls = Class.forName(className);
		return newObject(cls,param);
	}
	
	public static Object newInstance(Class<?> cls, Object... param) 
	throws Exception{
		if(cls==null) return null;
		if(param==null) return cls.newInstance();
		Object[] params = new Object[param.length];
		for(int i=0; i<param.length; i++){
			params[i] = param[i];
		}
		return newObject(cls,params);
	}
	
	public static Object newInstance(String className, Object... param) 
	throws Exception{
		if(className==null) return null;
		Class<?> cls = Class.forName(className);
		return newInstance(cls,param);
	}
	
	public static Constructor<?> getConstructor(Class<?> cls, Class<?>... parmaTypes)throws Exception{
		Exception ex = null;
		try{
			Constructor<?> con = cls.getConstructor(parmaTypes);
			return con;
		}catch (SecurityException e) {
			ex = e;
		}catch (NoSuchMethodException e) {
			ex = e;
		}
		Constructor<?>[] cons = cls.getConstructors();
		for(Constructor<?> con : cons){
			Class<?>[] conTypes = con.getParameterTypes();
			if(conTypes.length != parmaTypes.length) continue;
			int c = 0;
			for(int i=0; i<conTypes.length; i++){
				if( conTypes[i].toString().equals(parmaTypes[i].toString())||
				   ("class java.lang."+conTypes[i].toString()).equalsIgnoreCase(parmaTypes[i].toString())){
					 c ++;
					 continue;
				}
				break;
			}
			if(c==conTypes.length)return con;
		}
		throw ex;
	}
	
    public static Object invoke(Object obj, String method, Object... param)throws Exception{
    	try{
    		if(param == null){
    			try{ return obj.getClass().getMethod(method).invoke(obj);}catch(Exception e){throw e;}
    		}else{
    			try{
    				return getMethod(obj, method, param).invoke(obj,param);
    			}catch(Exception e){throw e;}
    		}
    	}catch(Exception e){
    		throw e;
    	}
    }
    
    public static Method getMethod(Object obj, String method, Object... param)throws Exception{
		Class<?>[] types = new Class[param.length];
		for(int i=0; i<param.length; i++)types[i] = param[i].getClass();
		return getMethod(obj, method, types);
    }
    
	public static boolean inherit(Class<?> input, Class<?> type){
		Class<?> t = forClass(type);
		Class<?> v = forClass(input);
		if(t==null && v==null)return true;
		if(t!=null && v==null||t==null && v!=null)return false;
		boolean isSame = false;
		Class<?> tmp = v;
		while(true){
			if(t.getCanonicalName().equals(tmp.getCanonicalName())){
				isSame = true;
				break;
			}
			tmp = tmp.getSuperclass();
			if(tmp==null) break;
		}
		if(isSame == false){
			isSame = inheritInterfaces(v, t);
		}
		return isSame;
	}
    
	public static boolean inheritInterfaces(Class<?> v, Class<?> t){
		if(t==null && v==null)return true;
		if(t!=null && v==null||t==null && v!=null)return false;
		if(v.getCanonicalName().equals(t.getCanonicalName())){
			return true;
		}
		Class<?>[] interFaces = v.getInterfaces();
		for(Class<?> inter : interFaces){
			if(inheritInterfaces(inter, t))return true;
		}
		return false;
	}
    
    public static Method getMethod(Object obj, String method, Class<?>... types)throws Exception{
    	Exception ex = null;
		try{
			return obj.getClass().getMethod(method,types);
		}catch(Exception e){ex=e;}
		Method[] methods = obj.getClass().getMethods();
		for(Method m : methods){
			if(!m.getName().equals(method))continue;
			Class<?>[] local = m.getParameterTypes();
			if(local.length != types.length )continue;
			int i=0;
			for(i=0; i<types.length; i++){
				boolean isSame = inherit(types[i], local[i]);
				if(!isSame)break;
			}
			if(i==types.length) return m;
		}
		throw ex;
    }
    
	//应用于Java中8个基本类型
	public static Object newObject(Class<?> classType, String value)throws Exception {
		 Exception ex = null;
		 try {
				return newObject(
						classType,
						new Class[]{String.class}, 
						new Object[]{value}
				);
		 } catch (Exception e) {
			 ex = e;
		 }
		//自定义的寻找
		 Object aside = asideObject(classType,value);
		 if(aside != null )return aside;
		 //自动寻找构造方法
		 Constructor<?>[] cons = classType.getConstructors();
		 for(Constructor<?> con : cons){
			 Class<?>[] types = con.getParameterTypes();
			 if(types.length!=1)continue;
			 Class<?> newClass = forClass(types[0]);
			 if(classType.getCanonicalName().equals(newClass.getCanonicalName()))continue;
			 try{
				 Object parma = newInstance(newClass,value);
				 return newInstance(classType,parma);
			 }catch(Exception e){
			 }
		 }
		 throw ex;
	}

	public static Object[] newObject(Class<?>[] classTypes, String[] values){
		Object[] objs = new Object[classTypes.length];
		if(classTypes==null || values==null || 
				classTypes.length != values.length) {
			return objs;
		}
		for(int i=0; i<classTypes.length; i++){
			try{
				objs[i] = newObject(classTypes[i], values[i]);
			}catch(Exception e){
			}
		}
		return objs;
	}
	
	public static Class<?> forClass(Class<?> cls){
		if(cls==null) return cls;
		String clsName = cls.toString().toLowerCase();
		try{
			if(!clsName.startsWith("class")){
				clsName = "java.lang."+clsName.substring(0,1).toUpperCase()+clsName.substring(1);
				return Class.forName(clsName);
			}
		}catch(Exception e){}
		return cls;
	}
	
	public static Object asideObject(Class<?> c, String value){
		if(value==null) return null;
		if(inherit(c, java.util.Date.class)){
			try{
				String[] arr = value.split("\\|");
				String v = arr[0].trim();
				String t = arr.length>1?arr[1].trim():"yyyy-MM-dd HH:mm:ss";
				java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(t);
				java.lang.Long l = sdf.parse(v).getTime();
				return newInstance(c,l);
			}catch(Exception e){}
		}
		return null;
	}
}

package chok.util;

import java.lang.reflect.Field;

public class ReflectionUtil
{
	public static String getAllFieldNames(Class<?> clazz)
	{
        return getAllFieldNames(clazz, "");  
	}
	
	public static String getAllFieldNames(Class<?> clazz, String names)
	{
		Field[] declaredFields = clazz.getDeclaredFields();
		for (Field field : declaredFields)
		{
			names += field.getName() + ",";
		}
		
		Class<?> superclass = clazz.getSuperclass();  
		if (superclass != null && !superclass.isAssignableFrom(Object.class)) 
		{// 简单的递归一下  
			return getAllFieldNames(superclass, names);  
		}  
		
		names = names.substring(0, names.length() - 1);
		return names;
	}
}

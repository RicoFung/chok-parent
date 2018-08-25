package chok.util;

import java.lang.reflect.Field;

public class ReflectionUtil
{
	public static String getAllFieldNames(Class<?> clazz)
	{
		String names = "";
		Field[] declaredFields = clazz.getDeclaredFields();
		for (Field field : declaredFields)
		{
			names += field.getName() + ",";
		}
		names = names.substring(0, names.length() - 1);
		return names;
	}
}

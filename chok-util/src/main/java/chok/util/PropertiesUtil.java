package chok.util;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesUtil
{
	static Logger log = LoggerFactory.getLogger(PropertiesUtil.class);
	
	private static Properties prop;

	/**
	 * 读取CLASSPATH：application.properties
	 * @param key 键值
	 * @return String
	 */
	public static String getValue(String key)
	{
		return getValue("", key);
	}
	
	/**
	 * 读取CLASSPATH：{dir}目录下的application.properties
	 * @param dir 目录路径（如："config/"）
	 * @param key 键值
	 * @return String
	 */
	public static String getValue(String dir, String key)
	{
		return getValue(dir, "application", null, key);
	}
	
	/**
	 * 读取CLASSPATH：{dir}目录下的application-{profile}.properties
	 * @param dir 目录路径（如："config/"）
	 * @param profile 环境
	 * @param key 键值
	 * @return String
	 */
	public static String getValue(String dir, String profile, String key)
	{
		return getValue(dir, "application", profile, key);
	}
	
	/**
	 * 读取CLASSPATH：{dir}目录下的{name}-{profile}.properties
	 * @param dir 目录路径（如："config/"）
	 * @param name 配置文件名称
	 * @param profile 环境
	 * @param key 键值
	 * @return String
	 */
	public static String getValue(String dir, String name, String profile, String key)
	{
		if (prop == null)
			prop = new Properties();
		try
		{
			if (null != profile && profile.length() > 0)
				prop.load(PropertiesUtil.class.getClassLoader().getResourceAsStream(dir + name + "-" + profile + ".properties"));
			else
				prop.load(PropertiesUtil.class.getClassLoader().getResourceAsStream(dir + name + ".properties"));
			return prop.getProperty(key);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			log.error(e.getMessage());
			return "";
		}
	}
}

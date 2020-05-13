package chok.common;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 *  从ApplicationContext取得spring管理的类
 *  默认情况下spring容器从org.springframework.web.context.ContextLoaderListener.getCurrentWebApplicationContext()中获取
 *  如果在非web应用中使用，则必须调用setApplicationContext方法进行设置
 * @author rico.fung
 *
 */
@Component
public class BeanFactory implements ApplicationContextAware
{
	private static ApplicationContext context;
	
	@Override
	public void setApplicationContext(ApplicationContext context)
	{
		BeanFactory.context = context;
	}
	
	/**
	 *  取得Spring托管的类
	 * @param name 托管类的Class
	 * @return Object
	 */
	public static Object getBean(Class<?> name)
	{
		return context.getBean(name);
	}
	
	/**
	 *  取得Spring托管的类
	 * @param name 托管类的ID
	 * @return Object
	 */
	public static Object getBean(String name)
	{
		return context.getBean(name);
	}
	
	/**
	 * 获取当前环境
	 * @return String
	 */
    public static String getActiveProfile() 
    {
        return context.getEnvironment().getActiveProfiles()[0];
    }
}
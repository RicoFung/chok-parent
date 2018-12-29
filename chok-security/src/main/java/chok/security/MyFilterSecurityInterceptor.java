package chok.security;

import java.io.IOException;
import java.util.Arrays;

import javax.annotation.PostConstruct;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.access.SecurityMetadataSource;
import org.springframework.security.access.intercept.InterceptorStatusToken;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

//@Component
/**
 * 此类作废
 * 由于自实现FilterSecurityInterceptor装配规则过滤器，web.ignoring().antMatchers不能ignore静态资源
 * 所以采用默认的FilterSecurityInterceptor装配规则过滤器，web.ignoring().antMatchers可以ignore静态资源
 * @author zhuojun.feng
 *
 */
public class MyFilterSecurityInterceptor extends FilterSecurityInterceptor implements Filter
{
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	// chok.security.ignore-uris
	private static String[] IGNORE_URIS = { "/", "/index*", "/**/home/menu", "/error", "/staticexternal/**", "/staticinternal/**" };

	@Autowired
	private Environment	env;
	@Autowired
	private FilterInvocationSecurityMetadataSource securityMetadataSource;
	
	@PostConstruct
	public void init()
	{
		String customIgnoreUris = env.getProperty("chok.security.ignore-uris");
		if (null != customIgnoreUris)
		{
			IGNORE_URIS = customIgnoreUris.trim().split(",");
		}
	}

	@Autowired
	public void setMyAccessDecisionManager(MyAccessDecisionManager myAccessDecisionManager)
	{
		super.setAccessDecisionManager(myAccessDecisionManager);
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException
	{
		if (log.isDebugEnabled())
			log.debug("---init---");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
	{
		if (log.isDebugEnabled())
			log.debug("---doFilter---");
		if (!isIgnoreUri((HttpServletRequest) request))
		{
			FilterInvocation fi = new FilterInvocation(request, response, chain);
			invoke(fi);
		}
		else
		{
			chain.doFilter(request, response);
		}
	}

	public void invoke(FilterInvocation fi) throws IOException, ServletException
	{
		if (log.isDebugEnabled())
		{
			log.debug("---invoke---");
			log.debug(fi.toString());
		}
		/**
		 * fi里面有一个被拦截的url
		 *  里面调用MyInvocationSecurityMetadataSource的getAttributes(Object object)
		 *  这个方法获取fi对应的所有权限
		 *  再调用MyAccessDecisionManager的decide方法来校验用户的权限是否足够
		 */
		InterceptorStatusToken token = super.beforeInvocation(fi);
		try
		{
			// 执行下一个拦截器
			fi.getChain().doFilter(fi.getRequest(), fi.getResponse());
		}
		finally
		{
			super.afterInvocation(token, null);
		}
	}

	@Override
	public void destroy()
	{

	}

	@Override
	public Class<?> getSecureObjectClass()
	{
		return FilterInvocation.class;
	}

	@Override
	public SecurityMetadataSource obtainSecurityMetadataSource()
	{
		return this.securityMetadataSource;
	}

	/**
	 * 校验忽略URI
	 * 
	 * @param req
	 * @return boolean
	 */
	private boolean isIgnoreUri(HttpServletRequest req)
	{
		boolean result = false;
		AntPathRequestMatcher uriMatcher;
		if (log.isDebugEnabled())
		{
			log.debug("IGNORE_URIS = {}", Arrays.toString(IGNORE_URIS));
			log.debug("REQUEST_URI = {}", req.getRequestURI());
		}
		for (int i = 0; i < IGNORE_URIS.length; i++)
		{
			String ignoreUri = IGNORE_URIS[i];
			uriMatcher = new AntPathRequestMatcher(ignoreUri);
			if (uriMatcher.matches(req))
			{
				result = true;
				break;
			}
		}
		if (log.isDebugEnabled())
		{
			log.debug("IS_IGNORE_URI = {}", result);
		}
		return result;
	}
}

package chok.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;

@Component
public class MyInvocationSecurityMetadataSourceService implements FilterInvocationSecurityMetadataSource
{
	private final Log log = LogFactory.getLog(getClass());

	// 将来用配置文件代替
	private final static String[] IGNORE_URIS = {"/","/index*","/error","/staticexternal/**","/staticinternal/**"};
	// 资源角色关系
	private HashMap<String, Collection<ConfigAttribute>> resRolesMap = null;

	/**
	 * 获得权限表中所有权限
	 */
	private void obtainResRolesMap()
	{
		resRolesMap = new HashMap<>();
		Collection<ConfigAttribute> roles = new ArrayList<ConfigAttribute>();
		roles.add(new SecurityConfig("ROLE_ADMIN"));
		roles.add(new SecurityConfig("ADMIN"));
		roles.add(new SecurityConfig("USER"));
		resRolesMap.put("/admin/home/**", roles);
		resRolesMap.put("/admin/category/query", roles);
		resRolesMap.put("/admin/category/query2", roles);
		resRolesMap.put("/admin/model/query", roles);
		log.info(resRolesMap.toString());
	}

	@Override
	public Collection<ConfigAttribute> getAllConfigAttributes()
	{
		return null;
	}

	/**
	 * 此方法是为了判定用户请求的url 是否在权限表中， 如果在权限表中，则返回给 decide 方法，用来判定用户是否有此权限。
	 * 如果不在权限表中，则白名单(放行)。
	 */
	@Override
	public Collection<ConfigAttribute> getAttributes(Object object) throws IllegalArgumentException
	{
		String reqURI;
		String resURI;
		AntPathRequestMatcher resURIMatcher;
		Collection<ConfigAttribute> resRoles;
		// 获取用户请求相对地址URI
		HttpServletRequest req = ((FilterInvocation) object).getHttpRequest();
		reqURI = req.getRequestURI().trim();
		if (req.getContextPath().length() > 0)
		{
			reqURI = reqURI.replaceFirst(req.getContextPath(), "");
		}
		if (log.isDebugEnabled())
			log.debug("REQURI = " + reqURI);
		// 获取权限列表
		if (resRolesMap == null)
			obtainResRolesMap();
		// 判断逻辑（匹配忽略则放行，否则校验角色授权）
		// 匹配忽略则放行
		if (isIgnoreUri(req))
		{
			if (log.isDebugEnabled())
				log.debug("IGNORE = " + reqURI);
			return null;
		}
		// 否则校验角色授权
		else
		{
			log.info("REQURI = " + reqURI);
			for (Entry<String, Collection<ConfigAttribute>> e : resRolesMap.entrySet())
			{
				resURI = e.getKey();
				resURIMatcher = new AntPathRequestMatcher(resURI);
				resRoles = e.getValue();
				if (resURIMatcher.matches(req))
				{
					log.info("URI matching [" + reqURI + "]");
					return resRoles;
				}
			}
			// 黑名单(会导致apache.coyote.http11.Http11Processor : Error processing request异常)
			return SecurityConfig.createList("BLACKLIST");
			// 白名单
			//		return null;
		}
	}

	@Override
	public boolean supports(Class<?> arg0)
	{
		return true;
	}

	/**
	 * 判断忽略URI
	 * @param req
	 * @return boolean
	 */
	private boolean isIgnoreUri(HttpServletRequest req)
	{
		AntPathRequestMatcher uriMatcher;
		for(String ignoreUri: IGNORE_URIS)
		{
			uriMatcher = new AntPathRequestMatcher(ignoreUri);
			if (uriMatcher.matches(req))
			{
				return true;
			}
		}
		return false;
	}
}

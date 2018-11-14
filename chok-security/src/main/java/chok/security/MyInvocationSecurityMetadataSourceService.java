package chok.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import chok.security.dto.TbAuthorityDto;
import chok.util.PropertiesUtil;

@Component
public class MyInvocationSecurityMetadataSourceService implements FilterInvocationSecurityMetadataSource
{
	private final Log log = LogFactory.getLog(getClass());

	// 读取ignore-uris
	private static String[] IGNORE_URIS = {"/","/index*","/error","/staticexternal/**","/staticinternal/**"};
	static 
	{
		String customIgnoreUris = PropertiesUtil.getValue("config/", "chok.security.ignore-uris");
		if (null != customIgnoreUris)
		{
			IGNORE_URIS	= customIgnoreUris.trim().split(",");
		}
	}
	// 资源角色关系
	private HashMap<String, Collection<ConfigAttribute>> authorityRoleMap = null;
	
	@Autowired
	LoadBalancerClient	loadBalancerClient;
	@Autowired
	RestTemplate		restTemplate;

	/**
	 * 获得权限表中所有权限
	 */
	private void obtainResRolesMap()
	{
		// 通过微服务获取App授权
		ServiceInstance serviceInstance = loadBalancerClient.choose("eureka-client");
		String url = "http://" + serviceInstance.getHost() + ":" + serviceInstance.getPort() + "/authority/queryWithRoleByAppid?appid=3";
		log.info("Rest url => " + url);
		JSONObject jo = restTemplate.getForObject(url, JSONObject.class);
		log.info("Rest result <= " + jo);
		//
		if (jo.getBoolean("success"))
		{
			// JSONArray 转 List<TbAuthorityDto> 
			JSONArray authJsonArray = jo.getJSONObject("data").getJSONArray("authorities");
			String js = JSONArray.toJSONString(authJsonArray, SerializerFeature.EMPTY);
			List<TbAuthorityDto> authorities = JSON.parseArray(js, TbAuthorityDto.class);
			// 构建authorityRoleMap
			authorityRoleMap = new HashMap<>();
			authorities.forEach(authority->{
				Collection<ConfigAttribute> roles = new ArrayList<ConfigAttribute>();
				authority.getTcRoles().forEach(role->{
					roles.add(new SecurityConfig(role.getTcCode()));
				});
				authorityRoleMap.put(authority.getTcUrl(), roles);
			});
		}
		log.info(authorityRoleMap!=null?authorityRoleMap.toString():"");
	}

	// Demo	
//	private void obtainResRolesMap()
//	{
//		authorityRoleMap = new HashMap<>();
//		Collection<ConfigAttribute> roles = new ArrayList<ConfigAttribute>();
//		roles.add(new SecurityConfig("ROLE_ADMIN"));
//		roles.add(new SecurityConfig("ADMIN"));
//		roles.add(new SecurityConfig("USER"));
//		authorityRoleMap.put("/admin/home/**", roles);
//		authorityRoleMap.put("/admin/category/query", roles);
//		authorityRoleMap.put("/admin/category/query2", roles);
//		authorityRoleMap.put("/admin/model/query", roles);
//		log.info(authorityRoleMap.toString());
//	}

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
		Collection<ConfigAttribute> authorityRole;
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
		if (authorityRoleMap == null)
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
			for (Entry<String, Collection<ConfigAttribute>> e : authorityRoleMap.entrySet())
			{
				resURI = e.getKey();
				resURIMatcher = new AntPathRequestMatcher(resURI);
				authorityRole = e.getValue();
				if (resURIMatcher.matches(req))
				{
					log.info("URI matching [" + reqURI + "]");
					return authorityRole;
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
		for(int i=0; i<IGNORE_URIS.length; i++)//String ignoreUri: IGNORE_URIS)
		{
			String ignoreUri = IGNORE_URIS[i];
			log.info("IGNORE_URIS[" + i + "] = " + ignoreUri);
			uriMatcher = new AntPathRequestMatcher(ignoreUri);
			if (uriMatcher.matches(req))
			{
				return true;
			}
		}
		return false;
	}
}

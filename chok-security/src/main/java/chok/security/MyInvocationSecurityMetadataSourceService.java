package chok.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	private final Logger log = LoggerFactory.getLogger(getClass());

	// chok.security.auth.service-id
	private static String AUTH_SERVICE_ID = "eureka-client";
	static
	{
		String customAuthServiceId = PropertiesUtil.getValue("config/", "chok.security.auth.service-id");
		if (null != customAuthServiceId)
		{
			AUTH_SERVICE_ID = customAuthServiceId.trim();
		}
	}
	// chok.security.auth.protocol
	private static String AUTH_PROTOCOL = "http";
	static
	{
		String customAuthProtocol = PropertiesUtil.getValue("config/", "chok.security.auth.protocol");
		if (null != customAuthProtocol)
		{
			AUTH_PROTOCOL = customAuthProtocol.trim();
		}
	}
	// chok.security.auth.uri
	private static String AUTH_URI = "/authority/queryWithRoleByAppId";
	static
	{
		String customAuthUri = PropertiesUtil.getValue("config/", "chok.security.auth.uri");
		if (null != customAuthUri)
		{
			AUTH_URI = customAuthUri.trim();
		}
	}
	// chok.security.auth.uri-key
	private static String AUTH_URI_KEY = "appId";
	static
	{
		String customAuthUriKey = PropertiesUtil.getValue("config/", "chok.security.auth.uri-key");
		if (null != customAuthUriKey)
		{
			AUTH_URI_KEY = customAuthUriKey.trim();
		}
	}
	// chok.security.auth.uri-value
	private static String AUTH_URI_VALUE = "";
	static
	{
		String customAuthUriValue = PropertiesUtil.getValue("config/", "chok.security.auth.uri-value");
		if (null != customAuthUriValue)
		{
			AUTH_URI_VALUE = customAuthUriValue.trim();
		}
	}
	// chok.security.ignore-uris
	private static String[] IGNORE_URIS = { "/", "/index*", "/**/home/menu", "/error", "/staticexternal/**",
			"/staticinternal/**" };
	static
	{
		String customIgnoreUris = PropertiesUtil.getValue("config/", "chok.security.ignore-uris");
		if (null != customIgnoreUris)
		{
			IGNORE_URIS = customIgnoreUris.trim().split(",");
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
		ServiceInstance serviceInstance = loadBalancerClient.choose(AUTH_SERVICE_ID);
		String url = AUTH_PROTOCOL + "://" + serviceInstance.getHost() + ":" + serviceInstance.getPort() + AUTH_URI
				+ "?" + AUTH_URI_KEY + "=" + AUTH_URI_VALUE;
		log.info("Rest url => {}", url);
		JSONObject jo = restTemplate.getForObject(url, JSONObject.class);
		log.info("Rest result <= {}", jo);
		//
		if (jo.getBoolean("success"))
		{
			// JSONArray 转 List<TbAuthorityDto>
			JSONArray authJsonArray = jo.getJSONObject("data").getJSONArray("authorities");
			String js = JSONArray.toJSONString(authJsonArray, SerializerFeature.EMPTY);
			List<TbAuthorityDto> authorities = JSON.parseArray(js, TbAuthorityDto.class);
			// 构建authorityRoleMap
			authorityRoleMap = new HashMap<>();
			authorities.forEach(authority ->
			{
				Collection<ConfigAttribute> roles = new ArrayList<ConfigAttribute>();
				authority.getTcRoles().forEach(role ->
				{
					roles.add(new SecurityConfig(role.getTcCode()));
				});
				authorityRoleMap.put(authority.getTcUrl(), roles);
			});
		}
		log.info(authorityRoleMap != null ? authorityRoleMap.toString() : "");
	}

	// Demo
	// private void obtainResRolesMap()
	// {
	// authorityRoleMap = new HashMap<>();
	// Collection<ConfigAttribute> roles = new ArrayList<ConfigAttribute>();
	// roles.add(new SecurityConfig("ROLE_ADMIN"));
	// roles.add(new SecurityConfig("ADMIN"));
	// roles.add(new SecurityConfig("USER"));
	// authorityRoleMap.put("/admin/home/**", roles);
	// authorityRoleMap.put("/admin/category/query", roles);
	// authorityRoleMap.put("/admin/category/query2", roles);
	// authorityRoleMap.put("/admin/model/query", roles);
	// log.info(authorityRoleMap.toString());
	// }

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
			log.debug("REQURI = {}", reqURI);
		// 获取权限列表
		if (authorityRoleMap == null)
			obtainResRolesMap();
		// 判断逻辑（匹配忽略则放行，否则校验角色授权）
		// 放行
		if (isIgnoreUri(req))
		{
			if (log.isDebugEnabled())
				log.debug("IGNORE = {}", reqURI);
			return null;
		}
		// 授权
		else
		{
			if (log.isDebugEnabled())
				log.debug("REQURI = {}", reqURI);
			for (Entry<String, Collection<ConfigAttribute>> e : authorityRoleMap.entrySet())
			{
				resURI = e.getKey();
				resURIMatcher = new AntPathRequestMatcher(resURI);
				authorityRole = e.getValue();
				if (resURIMatcher.matches(req))
				{
					if (log.isDebugEnabled())
						log.debug("URI matching [{}]", reqURI);
					return authorityRole;
				}
			}
			// 黑名单(会导致apache.coyote.http11.Http11Processor : Error processing request异常)
			return SecurityConfig.createList("BLACKLIST");
			// 白名单
			// return null;
		}
	}

	@Override
	public boolean supports(Class<?> arg0)
	{
		return true;
	}

	/**
	 * 校验忽略URI
	 * 
	 * @param req
	 * @return boolean
	 */
	private boolean isIgnoreUri(HttpServletRequest req)
	{
		AntPathRequestMatcher uriMatcher;
		if (log.isDebugEnabled())
			log.debug("IGNORE_URIS = {}", Arrays.toString(IGNORE_URIS));
		for (int i = 0; i < IGNORE_URIS.length; i++)
		{
			String ignoreUri = IGNORE_URIS[i];
			uriMatcher = new AntPathRequestMatcher(ignoreUri);
			if (uriMatcher.matches(req))
			{
				return true;
			}
		}
		return false;
	}
}

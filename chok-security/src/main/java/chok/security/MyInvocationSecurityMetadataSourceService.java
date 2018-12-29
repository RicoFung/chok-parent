package chok.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.core.env.Environment;
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

@Component
public class MyInvocationSecurityMetadataSourceService implements FilterInvocationSecurityMetadataSource
{
	private final Logger log = LoggerFactory.getLogger(getClass());

	private static String	AUTH_SERVICE_ID	= "eureka-client";
	private static String	AUTH_PROTOCOL	= "http";
	private static String	AUTH_URI		= "/authority/queryWithRoleByAppId";
	private static String	AUTH_URI_KEY	= "appId";
	private static String	AUTH_URI_VALUE	= "";

	// 资源角色关系
	private HashMap<String, Collection<ConfigAttribute>> AUTHORITY_ROLE_MAP = null;

	@Autowired
	private Environment	env;
	@Autowired
	private LoadBalancerClient	loadBalancerClient;
	@Autowired
	private RestTemplate		restTemplate;
	@Autowired
	private HttpSession			httpSession;

	@PostConstruct
	public void init()
	{
		String customAuthServiceId = env.getProperty("chok.security.auth.service-id");
		if (null != customAuthServiceId)
			AUTH_SERVICE_ID = customAuthServiceId.trim();
		String customAuthProtocol = env.getProperty("chok.security.auth.protocol");
		if (null != customAuthProtocol)
			AUTH_PROTOCOL = customAuthProtocol.trim();
		String customAuthUri = env.getProperty("chok.security.auth.uri");
		if (null != customAuthUri)
			AUTH_URI = customAuthUri.trim();
		String customAuthUriKey = env.getProperty("chok.security.auth.uri-key");
		if (null != customAuthUriKey)
			AUTH_URI_KEY = customAuthUriKey.trim();
		String customAuthUriValue = env.getProperty("chok.security.auth.uri-value");
		if (null != customAuthUriValue)
			AUTH_URI_VALUE = customAuthUriValue.trim();
	}
	
	/**
	 * 获得权限表中所有权限
	 */
	@SuppressWarnings("unchecked")
	private void obtainAuthorityRolesMap()
	{
		if (httpSession.getAttribute("AUTHORITY_ROLE_MAP") == null)
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
				AUTHORITY_ROLE_MAP = new HashMap<>();
				authorities.forEach(authority ->
				{
					Collection<ConfigAttribute> roles = new ArrayList<ConfigAttribute>();
					authority.getTcRoles().forEach(role ->
					{
						roles.add(new SecurityConfig(role.getTcCode()));
					});
					AUTHORITY_ROLE_MAP.put(authority.getTcUrl(), roles);
				});
			}
			log.info(AUTHORITY_ROLE_MAP != null ? AUTHORITY_ROLE_MAP.toString() : "");
			httpSession.setAttribute("AUTHORITY_ROLE_MAP", AUTHORITY_ROLE_MAP);
		}
		else
		{
			AUTHORITY_ROLE_MAP = (HashMap<String, Collection<ConfigAttribute>>)httpSession.getAttribute("AUTHORITY_ROLE_MAP");
		}
	}

	// Demo
//	 private void obtainAuthorityRolesMap()
//	 {
//		 AUTHORITY_ROLE_MAP = new HashMap<>();
//		 Collection<ConfigAttribute> roles = new ArrayList<ConfigAttribute>();
//		 roles.add(new SecurityConfig("ROLE_ADMIN"));
//		 roles.add(new SecurityConfig("ADMIN"));
//		 roles.add(new SecurityConfig("USER"));
//		 AUTHORITY_ROLE_MAP.put("/admin/home/**", roles);
//		 AUTHORITY_ROLE_MAP.put("/admin/category/query", roles);
//		 AUTHORITY_ROLE_MAP.put("/admin/category/query2", roles);
//		 AUTHORITY_ROLE_MAP.put("/admin/model/query", roles);
//		 log.info(AUTHORITY_ROLE_MAP.toString());
//	 }

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
//		if (AUTHORITY_ROLE_MAP == null)
		obtainAuthorityRolesMap();
		// 授权
		if (log.isDebugEnabled())
			log.debug("REQURI = {}", reqURI);
		for (Entry<String, Collection<ConfigAttribute>> e : AUTHORITY_ROLE_MAP.entrySet())
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

	@Override
	public boolean supports(Class<?> arg0)
	{
		return true;
	}
}

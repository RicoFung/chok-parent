package chok.security;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.FilterInvocation;
import org.springframework.stereotype.Component;

@Component
public class MyAccessDecisionManager implements AccessDecisionManager
{
	private final Logger log = LoggerFactory.getLogger(getClass());
	
  // decide 方法是判定是否拥有权限的决策方法，
  // authentication 是释CustomUserService中循环添加到 GrantedAuthority 对象中的权限信息集合.
  // object 包含客户端发起的请求的requset信息，可转换为 HttpServletRequest request = ((FilterInvocation) object).getHttpRequest();
  // configAttributes 为MyInvocationSecurityMetadataSource的getAttributes(Object object)这个方法返回的结果，此方法是为了判定用户请求的url 是否在权限表中，如果在权限表中，则返回给 decide 方法，用来判定用户是否有此权限。如果不在权限表中则放行。
	@Override
	public void decide(Authentication authentication, Object object, Collection<ConfigAttribute> configAttributes) throws AccessDeniedException, InsufficientAuthenticationException 
	{
    	if (log.isInfoEnabled())
    	{
			log.info(authentication.toString()+"");
			log.info("UserPrincipal == " + authentication.getPrincipal());
			log.info("UserRoles == " + authentication.getAuthorities());
			log.info("ResourceRoles == " + configAttributes.toString());
    	}
	    if(null == configAttributes || configAttributes.size() <=0) 
	    {
		    log.error("ResourceRoles is null");
		    throw new AccessDeniedException("ResourceRoles is null");
	    }
	    for(ConfigAttribute ca : configAttributes)
	    {
	        for(GrantedAuthority ga : authentication.getAuthorities()) 
	        {
	            if(ca.getAttribute().trim().equals(ga.getAuthority())) 
	            {
	            	if (log.isDebugEnabled())
	            		log.debug("Roles matching [{}]", ga.getAuthority());
	                return;
	            }
	        }
	    }
		// 获取用户请求相对地址URI
		HttpServletRequest req = ((FilterInvocation) object).getHttpRequest();
		String reqURI = req.getRequestURI().trim();
		if (req.getContextPath().length() > 0)
		{
			reqURI = reqURI.replaceFirst(req.getContextPath(), "");
		}
	    final String msg = ""
	    		+ "<div>"
	    		+ "Access Denied !<br/>"
	    		+ "NeedRoles=" + configAttributes.toString() + ";<br/>"
	    		+ "UserRoles=" + authentication.getAuthorities() + ";<br/>"
				+ "URI=" + reqURI
				+ "<div/>";
	    throw new AccessDeniedException(msg);
	}
	
	@Override
	public boolean supports(ConfigAttribute attribute) 
	{
	    return true;
	}
	
	@Override
	public boolean supports(Class<?> clazz) 
	{
	    return true;
	}
}

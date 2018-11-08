package chok.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class MyAuthenticationEntryPoint implements AuthenticationEntryPoint 
{
	private final Log log = LogFactory.getLog(getClass());
	
	@Autowired
	AjaxRequestMatcher ajaxRequestMatcher;
	
	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException 
	{
		if (!response.isCommitted())
		{
			if (ajaxRequestMatcher.matches(request))
			{
				log.info("(ajax: " + request.getRequestURI().trim() + ")(401)(" + authException.getMessage() + ")");
			}
			else
			{
				log.info("(!ajax: " + request.getRequestURI().trim() + ")(401)(" + authException.getMessage() + ")");
			}
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
		}
	}
}

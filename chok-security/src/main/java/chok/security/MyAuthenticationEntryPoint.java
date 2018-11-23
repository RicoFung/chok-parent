package chok.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class MyAuthenticationEntryPoint implements AuthenticationEntryPoint 
{
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Autowired
	AjaxRequestMatcher ajaxRequestMatcher;
	
	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException 
	{
		if (!response.isCommitted())
		{
			if (ajaxRequestMatcher.matches(request))
			{
				if (log.isInfoEnabled())
					log.info("(ajax: ({})(401)({})", request.getRequestURI().trim(), authException.getMessage());
			}
			else
			{
				if (log.isInfoEnabled())
					log.info("(!ajax: ({})(401)({})", request.getRequestURI().trim(), authException.getMessage());
			}
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
		}
	}
}

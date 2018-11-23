package chok.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class MyAccessDeniedHandler implements AccessDeniedHandler
{
	private final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	AjaxRequestMatcher ajaxRequestMatcher;

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException, ServletException
	{
		if (!response.isCommitted())
		{
			if (ajaxRequestMatcher.matches(request))
			{
				log.error("(ajax)(403)({})", accessDeniedException.getMessage());
				response.sendError(HttpServletResponse.SC_FORBIDDEN, accessDeniedException.getMessage());
			}
			else
			{
				log.error("(!ajax)(403)({})", accessDeniedException.getMessage());
				request.setAttribute("msg", accessDeniedException.getMessage());
				request.getRequestDispatcher("/error/403").forward(request, response);
			}
		}
	}
}

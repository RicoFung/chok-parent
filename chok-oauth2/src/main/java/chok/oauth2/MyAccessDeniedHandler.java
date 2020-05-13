package chok.oauth2;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import chok.common.RestResult;

public class MyAccessDeniedHandler implements AccessDeniedHandler
{
	private final Logger log = LoggerFactory.getLogger(getClass());

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException, ServletException
	{
		ObjectMapper objectMapper = new ObjectMapper();
		// 组装数据
		RestResult result = new RestResult();
		result.setSuccess(false);
		result.setCode("402");
		result.setMsg(accessDeniedException.getMessage());
		result.put("exception", accessDeniedException.getCause()==null?accessDeniedException.getMessage():accessDeniedException.getCause());
		result.setPath(request.getServletPath());
		result.setTimestamp(String.valueOf(new Date().getTime()));
		// 返回数据
		response.setContentType("application/json;charset=UTF-8");
		response.setStatus(402);
		try
		{
			objectMapper.writeValue(response.getOutputStream(), result);
		}
		catch (Exception e)
		{
			log.error(objectMapper.writeValueAsString(e));
		}
		finally
		{
			log.error(objectMapper.writeValueAsString(result));
		}
	}
}

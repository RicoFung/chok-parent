package chok.oauth2;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import com.fasterxml.jackson.databind.ObjectMapper;

import chok.common.RestResult;

public class MyOAuth2ExceptionEntryPoint implements AuthenticationEntryPoint
{
	private static Logger log = LoggerFactory.getLogger(MyOAuth2ExceptionEntryPoint.class);

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException
	{
		ObjectMapper objectMapper = new ObjectMapper();
		// 组装数据
		RestResult result = new RestResult();
		result.setSuccess(false);
		result.setCode("401");
		result.setMsg(authException.getMessage());
		result.put("exception", authException.getCause());
		result.setPath(request.getServletPath());
		result.setTimestamp(String.valueOf(new Date().getTime()));
		// 返回数据
		response.setContentType("application/json;charset=UTF-8");
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
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

package chok.jwt;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import io.jsonwebtoken.Claims;

@Component
public class JwtInterceptor implements HandlerInterceptor
{

	private final Logger	log	= LoggerFactory.getLogger(getClass());
	@Autowired
	private JwtConfig		jwtConfig;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
	{
		JSONObject restResult = new JSONObject();
		restResult.put("success", true);
		restResult.put("msg", "");

		if (HttpMethod.OPTIONS.equals(request.getMethod()))
		{
			response.setStatus(HttpServletResponse.SC_OK);
			return true;
		}

		final String authHeader = request.getHeader(JwtConstant.AUTH_HEADER_KEY);
		// 校验请求头是否配置Authorization
		if (StringUtils.isEmpty(authHeader))
		{
			restResult.put("msg", JwtErrorType.USER_NO_AUTHENTICATED);
			restResult.put("success", false);
			sendJsonMessage(response, restResult);
			log.error(JwtErrorType.USER_NO_AUTHENTICATED.toString());
			return false;
		}

		// 校验请求头格式
		if (!JwtUtil.validate(authHeader))
		{
			restResult.put("msg", JwtErrorType.AUTHORIZATION_HEADER_INCORRECT);
			restResult.put("success", false);
			sendJsonMessage(response, restResult);
			log.error(JwtErrorType.AUTHORIZATION_HEADER_INCORRECT.toString());
			return false;
		}

		// token解析
		final String authToken = JwtUtil.getRawToken(authHeader);
		Claims claims = null;
		try
		{
			claims = JwtUtil.parseToken(authToken, jwtConfig.getBase64Secret());
		}
		catch (Exception e) 
		{
			restResult.put("msg", e.getMessage());
			restResult.put("success", false);
			sendJsonMessage(response, restResult);
			log.error(e.getMessage());
			return false;
		}

		// 传递所需信息
		request.setAttribute("CLAIMS", claims);
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception
	{

	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception
	{

	}

	private void sendJsonMessage(HttpServletResponse response, Object obj) throws Exception
	{
		response.setContentType("application/json; charset=utf-8");
		PrintWriter writer = response.getWriter();
		writer.print(JSONObject.toJSONString(obj, SerializerFeature.WriteMapNullValue,
				SerializerFeature.WriteDateUseDateFormat));
		writer.close();
		response.flushBuffer();
	}
}
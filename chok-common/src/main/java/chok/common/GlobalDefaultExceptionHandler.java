package chok.common;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class GlobalDefaultExceptionHandler
{
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@ExceptionHandler(value = Exception.class)
	@ResponseBody
	public RestResult defaultErrorHandler(HttpServletRequest req, Exception e)
	{
		// 打印异常信息：
		e.printStackTrace();
		String exceptionCasuse = e.getCause() + "\r\n" + e.toString();
		StringBuffer exceptionStack = new StringBuffer();
		Arrays.asList(e.getStackTrace()).stream().forEach(x -> exceptionStack.append(x.toString() + "\r\n"));
		log.error("\r\n####################################################################################################"
				+ "\r\n# 捕获全局异常  "
				+ "\r\n####################################################################################################"
				+ "\r\n# ExceptionCasuse #:"
				+ "\r\n{}"
				+ "\r\n# ExceptionStack #:"
				+ "\r\n{}",
				exceptionCasuse, exceptionStack);
		RestResult r = new RestResult();
		r.setSuccess(false);
		r.setMsg(e.toString());
		return r;
	}
}

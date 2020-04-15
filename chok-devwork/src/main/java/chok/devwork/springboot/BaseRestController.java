package chok.devwork.springboot;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ModelAttribute;

import chok.common.RestResult;

public class BaseRestController<T>
{
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	protected RestResult restResult;
	
	@ModelAttribute
	public void BaseInitialization(HttpServletRequest request, HttpServletResponse response)
	{
		this.restResult = new RestResult();
	}
	
}

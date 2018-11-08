package chok.security;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;

@Component
public class AjaxRequestMatcher implements RequestMatcher 
{
    @Override
    public boolean matches(HttpServletRequest request) 
    {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With")) || request.getHeader("Accept") != null && request.getHeader("Accept").contains("application/json");
    }
}

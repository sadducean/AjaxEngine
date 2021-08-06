package sol.haruzion.ajaxengine.interceptor;

import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class AuthInterceptor implements HandlerInterceptor{
	@Resource(name="hostlist") List<String> hostlist;
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		try {
			String host=request.getHeader(HttpHeaders.HOST).split(":")[0].toLowerCase();
			boolean allowed=false;
			for (String allowedhost : hostlist) {
				if(allowedhost.equals(host)) {
					allowed=true;
					break;
				}
			}
			if(!allowed) {
				response.sendError(HttpStatus.FORBIDDEN.value(),"허용되지 않는 요청");
				return false;
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		
	}

}

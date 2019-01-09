package com.monkeyk.sos.config;

import java.io.IOException;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.catalina.Session;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
@Component
public class MylogoutSuccessHandler implements LogoutSuccessHandler{

	@Override
	public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
			throws IOException, ServletException {
		String sso = CookieUtil.getCookieByName(request, OauthConstants.SSO_ACCESS_TOKEN_NAME);
		for (Entry<String, Object> t : MyJwtTokenStore.map.entrySet()) {
			if(t.getValue().equals(sso))
			{
				MyJwtTokenStore.map.remove(t.getKey());

			}
			
		}
		
		Authentication authentication2 = (Authentication) request.getAttribute("authentication");
		
		System.err.println("注销成功处理"+JSONObject.toJSONString(authentication));
		HttpSession s = request.getSession();
		response.sendRedirect(request.getParameter("redirect_uri")==null? "/":request.getParameter("redirect_uri"));
		//new ForwardLogoutSuccessHandler(request.getParameter("redirect_uri")).onLogoutSuccess(request, response, authentication);
	}

}

package cn.merryyou.sso.client;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Token过滤器
 *
 * @author hackyo Created on 2017/12/8 9:28.
 */
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		try {
			OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
			if(null==authentication) {
				chain.doFilter(request, response);
				return ;
			}
			RestTemplate restTemplate = new RestTemplate();
			MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
			OAuth2AuthenticationDetails  aut =  (OAuth2AuthenticationDetails) authentication.getDetails();
			String url = "http://localhost:8080/m/tt/token?";
			paramMap.add("id",aut.getTokenValue());
			OAuth2RestTemplate ss = SpringUtil.getBean(OAuth2RestTemplate.class);
			//	client_id=springboot-client2&client_secret=12345678&grant_type=authorization_code&code=RIwTil&redirect_uri=http://localhost:8083/ui2/login
			// String json = SpringUtil.getBean(OAuth2RestTemplate.class).postForObject(url, paramMap, String.class);
			 String json =ss.postForObject(url, paramMap, String.class);
 
			if(null == json) {
				 request.getRequestDispatcher("/logout").forward(request, response);
			 }else {
				 chain.doFilter(request, response);
			 }
			 System.err.println(json);
		} catch (RestClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
	}

}
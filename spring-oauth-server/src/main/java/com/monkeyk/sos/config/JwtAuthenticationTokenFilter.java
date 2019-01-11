package com.monkeyk.sos.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import com.alibaba.fastjson.JSONObject;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map.Entry;

/**
 * Token过滤器
 *
 * @author hackyo Created on 2017/12/8 9:28.
 */
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

	private UserDetailsService userDetailsService;
	private JwtTokenUtil jwtTokenUtil;

	@Autowired
	public JwtAuthenticationTokenFilter(UserDetailsService userDetailsService, JwtTokenUtil jwtTokenUtil) {
		this.userDetailsService = userDetailsService;
		this.jwtTokenUtil = jwtTokenUtil;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String url = request.getRequestURI();
		System.err.println("请求路径：" + url);

		Enumeration<String> tn = request.getParameterNames();
		while (tn.hasMoreElements()) {
			String as = tn.nextElement();
			System.err.println(as + "-->" + request.getParameter(as));

		}
		MyJwtTokenStore.map.get("");
		for (Entry<String, Object> t : MyJwtTokenStore.map.entrySet()) {
			System.err.println("key: "+t.getKey()+":");
			System.err.println(t.getValue());
			
		}
		String authToken = CookieUtil.getCookieByName(request, OauthConstants.SSO_ACCESS_TOKEN_NAME);
		if (authToken != null) {
			String username = jwtTokenUtil.getUsernameFromToken(authToken);
			if (username != null ) {
				UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
				if (jwtTokenUtil.validateToken(authToken, userDetails)) {
					UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
							userDetails, userDetails.getPassword(), userDetails.getAuthorities());
					authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(authentication);
					
				}
			}
		}
		System.out.println("sessionid:"+request.getSession(false));
		chain.doFilter(request, response);
	}

}
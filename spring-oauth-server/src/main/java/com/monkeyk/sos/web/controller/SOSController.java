package com.monkeyk.sos.web.controller;

import java.io.IOException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.monkeyk.sos.config.JwtTokenUtil;
import com.monkeyk.sos.config.MyJwtTokenStore;
import com.monkeyk.sos.web.WebUtils;

/**
 * 2018/4/19
 * <p>
 * starup
 *
 * @author Shengzhao Li
 */
@Controller
public class SOSController {
	@Autowired
	private AuthenticationManager authenticationManager;
	@Autowired
	private UserDetailsService userDetailsService;
	@Autowired
	private JwtTokenUtil jwtTokenUtil;
	private static final Logger LOG = LoggerFactory.getLogger(SOSController.class);
	@Autowired
	private TokenStore tokenStore;

	/**
	 * 首页
	 */
	@RequestMapping(value = "/")
	public String index(Model model) {
		return "index";
	}

	// Go login
	@GetMapping(value = { "/login" })
	public String login(Model model) {
		LOG.info("Go to login, IP: {}", WebUtils.getIp());
		return "login";
	}

	@RequestMapping("/signin")
	public Object signin(String username, String password, HttpServletRequest req, HttpServletResponse res) {
		try {UsernamePasswordAuthenticationToken upToken = new UsernamePasswordAuthenticationToken(username, password);
		Authentication authentication = authenticationManager.authenticate(upToken);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		UserDetails userDetails = userDetailsService.loadUserByUsername(username);
		String tooken = jwtTokenUtil.generateToken(userDetails);
		res.addHeader("Authorization", tooken);
		res.addCookie(new Cookie("Authorization", tooken));
		
			res.sendRedirect("/");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}
	
	 @RequestMapping("/exit")
	    public void exit(HttpServletRequest request, HttpServletResponse response) {
	        // token can be revoked here if needed
	        new SecurityContextLogoutHandler().logout(request, null, null);
	        try {
	            //sending back to client app
	        	System.err.println(request.getHeader("referer"));
	            response.sendRedirect(request.getHeader("referer"));
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	 
	
}

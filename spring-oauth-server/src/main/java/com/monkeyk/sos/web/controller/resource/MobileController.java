package com.monkeyk.sos.web.controller.resource;

import com.monkeyk.sos.service.dto.UserJsonDto;
import com.monkeyk.sos.config.MyJwtTokenStore;
import com.monkeyk.sos.service.UserService;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.ConsumerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Shengzhao Li
 */
@Controller
@RequestMapping("/m/")
public class MobileController {
	@Autowired
	private TokenStore tokenStore;
	@Autowired
	private AuthorizationServerTokenServices authorizationServerTokenServices;

	@Autowired
	private ConsumerTokenServices consumerTokenServices;

    @Autowired
    private UserService userService;


    @RequestMapping("dashboard")
    public String dashboard() {
        return "mobile/dashboard";
    }

    @RequestMapping("user_info")
    @ResponseBody
    public UserJsonDto userInfo() {
		OAuth2Authentication oAuth2Authentication = 	(OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
		OAuth2AccessToken accessToken = authorizationServerTokenServices.getAccessToken(oAuth2Authentication);

        return userService.loadCurrentUserJsonDto();
    }

    @RequestMapping("/logout")
	public Object logout(Principal principal, HttpServletRequest request, HttpServletResponse response) {
		Authentication oAuth2Aqwuthentication =  SecurityContextHolder.getContext().getAuthentication();

		//OAuth2Authentication oAuth2Authentication = (OAuth2Authentication) principal;
		OAuth2Authentication oAuth2Authentication = 	(OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
		OAuth2AccessToken accessToken = authorizationServerTokenServices.getAccessToken(oAuth2Authentication);
		consumerTokenServices.revokeToken(accessToken.getValue());
		return null;

	}
    @RequestMapping("/tt/token")
	 @ResponseBody
	 public Object getToken(@RequestParam("id")String id) {
		return MyJwtTokenStore.map.get(id);
	 }
}
package com.monkeyk.sos.config;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class CustomTokenEnhancer implements TokenEnhancer {
	@Override
    public OAuth2AccessToken enhance(
     OAuth2AccessToken accessToken, 
     OAuth2Authentication authentication) {
        Map<String, Object> additionalInfo = new HashMap<>();
        HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
        String code =  request.getParameter("code");
        
        if(MyJwtTokenStore.map.containsKey(code)) {
        	additionalInfo.put("sso", MyJwtTokenStore.map.get(code));
        	MyJwtTokenStore.map.remove(code);
        }
        ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(additionalInfo);
        return accessToken;
    }
}
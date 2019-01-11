package com.monkeyk.sos.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.MacSigner;
import org.springframework.security.jwt.crypto.sign.SignatureVerifier;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import com.alibaba.fastjson.JSONObject;
import com.monkeyk.sos.SpringUtil;
/**
 * token 和ssotoken对应
 * @author Administrator
 *
 */
public class MyJwtTokenStore extends JwtTokenStore{
	public final static Map<String , Object> map = new ConcurrentHashMap<>();

	public MyJwtTokenStore(JwtAccessTokenConverter jwtTokenEnhancer) {
		super(jwtTokenEnhancer);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void storeAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication) {
		Authentication oAuth2Authentication =  SecurityContextHolder.getContext().getAuthentication();
    	SignatureVerifier verifier = new MacSigner("123");
		 Jwt a = JwtHelper.decodeAndVerify(token.getValue(),verifier);
		System.err.println(a);
		DataSource d = SpringUtil.getBean(DataSource.class);
		new JdbcTokenStore(d).storeAccessToken(token, authentication);
		System.err.println("存储"+JSONObject.toJSONString(token));
		String claims = a.getClaims();
		JSONObject claimsJO = JSONObject.parseObject(claims);
		System.err.println(claimsJO.getString("user_name"));
		
		map.put(token.getValue(), claimsJO.getString("sso"));

	}

}

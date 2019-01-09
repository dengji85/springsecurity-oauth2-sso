package com.monkeyk.sos.config;

import java.util.Arrays;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.approval.UserApprovalHandler;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.JdbcAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

import com.monkeyk.sos.domain.oauth.CustomJdbcClientDetailsService;
import com.monkeyk.sos.service.OauthService;
import com.monkeyk.sos.service.UserService;
import com.monkeyk.sos.web.oauth.OauthUserApprovalHandler;

/**
 * 2018/2/8
 * <p>
 * <p>
 * OAuth2 config
 *
 * @author Shengzhao Li
 */
@Configuration
public class OAuth2ServerConfiguration {

	/* Fixed, resource-id */
	public static final String RESOURCE_ID = "sos-resource";
	@Autowired
	private TokenStore tokenStore;
	// unity resource
	@Configuration
	@EnableResourceServer
	protected  class UnityResourceServerConfiguration extends ResourceServerConfigurerAdapter {
		

		@Override
		public void configure(ResourceServerSecurityConfigurer resources) {
			resources.resourceId(RESOURCE_ID).stateless(false);
			resources.tokenStore(tokenStore);
		}

		@Override
		public void configure(HttpSecurity http) throws Exception {
			http
					// Since we want the protected resources to be accessible in the UI as well we
					// need
					// session creation to be allowed (it's disabled by default in 2.0.6)
					//.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED).and()
					.requestMatchers().antMatchers("/unity/**").and().authorizeRequests().antMatchers("/unity/**")
					.access("#oauth2.hasScope('read') and hasRole('ROLE_UNITY')");

		}

	}

	// mobile resource
	@Configuration
	@EnableResourceServer
	protected class MobileResourceServerConfiguration extends ResourceServerConfigurerAdapter {

		@Override
		public void configure(ResourceServerSecurityConfigurer resources) {
			resources.resourceId(RESOURCE_ID).stateless(false);
			resources.tokenStore(tokenStore);
			System.err.println(tokenStore);
		}


		@Override
		public void configure(HttpSecurity http) throws Exception {
			http
					// Since we want the protected resources to be accessible in the UI as well we
					// need
					// session creation to be allowed (it's disabled by default in 2.0.6)
					//.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED).and()
					.requestMatchers().antMatchers("/m/**").and().authorizeRequests().antMatchers("/m/**")
					.access("#oauth2.hasScope('read') and hasRole('ROLE_MOBILE')");
			 http.authorizeRequests()
             .antMatchers("/m/user_info").permitAll();
		}

	}

	@Configuration
	@EnableAuthorizationServer
	protected static class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

		@Autowired
		private TokenStore tokenStore;

		@Autowired
		private ClientDetailsService clientDetailsService;

		@Autowired
		private OauthService oauthService;

		@Autowired
		private UserService userDetailsService;

		@Autowired
		@Qualifier("authenticationManagerBean")
		private AuthenticationManager authenticationManager;
		@Autowired
		private DataSource dataSource;

		@Override
		public void configure(ClientDetailsServiceConfigurer clients) throws Exception {

			clients.withClientDetails(clientDetailsService);
		}

		/*
		 * @Bean public TokenStore tokenStore(DataSource dataSource) { return new
		 * JdbcTokenStore(dataSource); }
		 */

		@Bean
		public ClientDetailsService clientDetailsService(DataSource dataSource) {
			return new CustomJdbcClientDetailsService(dataSource);
		}

		@Bean
		public AuthorizationCodeServices authorizationCodeServices(DataSource dataSource) {
			return new MyJdbcAuthorizationCodeServices(dataSource);
		}

		@Override
		public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
			
			 TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
			    tokenEnhancerChain.setTokenEnhancers(
			      Arrays.asList(tokenEnhancer(), accessTokenConverter()));
			    endpoints.tokenStore(tokenStore()).tokenEnhancer(tokenEnhancerChain).authorizationCodeServices(authorizationCodeServices(dataSource))
				.userDetailsService(userDetailsService).userApprovalHandler(userApprovalHandler())
				.authenticationManager(authenticationManager);
			    
		}
		@Bean
		public TokenEnhancer tokenEnhancer() {
		    return new CustomTokenEnhancer();
		}
		@Bean
		public TokenStore tokenStore() {
			return new MyJwtTokenStore(accessTokenConverter());
		}

		@Bean
		public JwtAccessTokenConverter accessTokenConverter() {
			
			JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
			converter.setSigningKey("123");
			return converter;
		}

		@Override
		public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
			oauthServer.realm("spring-oauth-server").allowFormAuthenticationForClients();
			oauthServer.tokenKeyAccess("isAuthenticated()");
		    	//客户端模式必须要
		}

		@Bean
		public OAuth2RequestFactory oAuth2RequestFactory() {
			return new DefaultOAuth2RequestFactory(clientDetailsService);
		}

		@Bean
		public UserApprovalHandler userApprovalHandler() {
			OauthUserApprovalHandler userApprovalHandler = new OauthUserApprovalHandler();
			userApprovalHandler.setOauthService(oauthService);
			System.err.println(tokenStore);
			userApprovalHandler.setTokenStore(tokenStore);
			userApprovalHandler.setClientDetailsService(this.clientDetailsService);
			userApprovalHandler.setRequestFactory(oAuth2RequestFactory());
			return userApprovalHandler;
		}

	}

}
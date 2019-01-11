package cn.merryyou.sso.client;

import javax.servlet.Filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created on 2017/12/26.
 *
 * @author zlf
 * @since 1.0
 */@Configuration
@SpringBootApplication
@RestController
@EnableOAuth2Sso
@EnableWebSecurity
public class SsoClient3Application extends WebSecurityConfigurerAdapter{

    @Autowired
    OAuth2RestTemplate oAuth2RestTemplate;

    @GetMapping("/user")
    public Authentication user(Authentication user) {
        return user;
    }

    @Value("${messages.url:http://127.0.0.1:8085}/resource/api")
    String messagesUrl;
    @Autowired
	private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    public static void main(String[] args) {
       ConfigurableApplicationContext app = SpringApplication.run(SsoClient3Application.class, args);
        SpringUtil.setApplicationContext(app);
    }

    @RequestMapping("/api")
    String home(Model model) {
        String result = oAuth2RestTemplate.getForObject(messagesUrl + "/2", String.class);
        return result;
    }

    @Bean
    OAuth2RestTemplate oAuth2RestTemplate(OAuth2ClientContext oAuth2ClientContext, OAuth2ProtectedResourceDetails details){
        return new OAuth2RestTemplate(details,oAuth2ClientContext);
    }
    @Override
    protected void configure(HttpSecurity http) throws Exception {
    	http.csrf().disable();
    	http.authorizeRequests().antMatchers("/login*").permitAll()
    	.anyRequest().authenticated();
    	http.logout().logoutUrl("/logout").deleteCookies("JSESSIONID").logoutSuccessUrl("http://sso.com:8080/signout?redirect_uri=http://c3.com:8083/c3");
        http.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

    }
}

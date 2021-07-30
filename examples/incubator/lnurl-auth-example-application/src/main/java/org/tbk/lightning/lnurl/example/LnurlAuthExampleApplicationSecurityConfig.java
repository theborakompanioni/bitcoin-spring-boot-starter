package org.tbk.lightning.lnurl.example;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@EnableWebSecurity
@Configuration(proxyBeanMethods = false)
public class LnurlAuthExampleApplicationSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors().and()
                .csrf().disable()
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation().changeSessionId()
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(true)
                )
                .authorizeRequests(authorize -> authorize
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .antMatchers("/index.html").permitAll()
                        .antMatchers("/authenticated.html").authenticated()
                        .antMatchers("/login").anonymous()
                        .antMatchers("/api/v1/lnauth/**").anonymous()
                        .anyRequest().authenticated()
                )
                .headers()
                .xssProtection()
                .xssProtectionEnabled(true)
                .block(true)
                .and()
                //.frameOptions().deny()
                //.contentTypeOptions()
                //.and()
                .contentSecurityPolicy("script-src 'self'");
    }
}

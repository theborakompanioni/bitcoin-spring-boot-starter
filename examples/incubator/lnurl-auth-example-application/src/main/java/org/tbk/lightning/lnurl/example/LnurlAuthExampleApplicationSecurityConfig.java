package org.tbk.lightning.lnurl.example;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
@Configuration(proxyBeanMethods = false)
public class LnurlAuthExampleApplicationSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors().and()
                .csrf().disable()
                .authorizeRequests()
                .anyRequest().permitAll()
                .and()
                .headers()
                .xssProtection()
                .xssProtectionEnabled(true)
                .block(true)
                .and()
                .contentSecurityPolicy("script-src 'self'");
    }
}

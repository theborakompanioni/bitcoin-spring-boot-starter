package org.tbk.bitcoin.duckdca.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.SecurityFilterChain;

@Slf4j
@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
class BitcoinDuckDcaExampleWebSecurityConfig implements WebSecurityCustomizer {

    @Override
    public void customize(WebSecurity web) {
        web
                .ignoring()
                .antMatchers("/webjars/**")
                .antMatchers("/index.html")
                .antMatchers("/favicon.png")
                .antMatchers("/favicon.ico")
                .antMatchers("/css/**")
                .antMatchers("/fonts/**")
                .antMatchers("/images/**")
                .antMatchers("/js/**");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors().and()
                .csrf().disable()
                .authorizeRequests()
                .anyRequest().permitAll();

        return http.build();
    }
}
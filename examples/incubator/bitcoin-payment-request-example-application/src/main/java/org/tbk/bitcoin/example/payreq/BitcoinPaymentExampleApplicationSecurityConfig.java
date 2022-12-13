package org.tbk.bitcoin.example.payreq;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter.HeaderValue;

@EnableWebSecurity
@Configuration(proxyBeanMethods = false)
public class BitcoinPaymentExampleApplicationSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors().and()
                .csrf().disable()
                .authorizeHttpRequests()
                .anyRequest().permitAll()
                .and()
                .headers(headers -> headers
                        .xssProtection(xss -> xss.headerValue(HeaderValue.ENABLED_MODE_BLOCK))
                        .contentSecurityPolicy("script-src 'self'")
                );

        return http.build();
    }
}

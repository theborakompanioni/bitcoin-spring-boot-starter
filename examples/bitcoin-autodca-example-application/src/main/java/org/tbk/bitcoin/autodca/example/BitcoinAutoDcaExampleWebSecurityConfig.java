package org.tbk.bitcoin.autodca.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.security.autoconfigure.web.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@Slf4j
@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@ConditionalOnWebApplication
class BitcoinAutoDcaExampleWebSecurityConfig implements WebSecurityCustomizer {

    @Override
    public void customize(WebSecurity web) {
        web.httpFirewall(new StrictHttpFirewall());
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> {
                })
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.requestMatchers(
                        PathRequest.toStaticResources().atCommonLocations(),
                        PathPatternRequestMatcher.withDefaults().matcher("/index.html"),
                        PathPatternRequestMatcher.withDefaults().matcher("/fonts/**")
                ).permitAll())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }
}
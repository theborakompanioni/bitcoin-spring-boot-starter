package org.tbk.spring.lnurl.security.test.app1;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.tbk.lnurl.auth.K1Manager;
import org.tbk.spring.lnurl.security.LnurlAuthConfigurer;
import org.tbk.spring.lnurl.security.userdetails.LnurlAuthUserPairingService;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Slf4j
@EnableWebSecurity
@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
class LnurlAuthTestSecurityConfig implements WebSecurityCustomizer {

    @NonNull
    private final LnurlAuthUserPairingService pairingService;

    @NonNull
    private final K1Manager k1Manager;

    @Override
    public void customize(WebSecurity web) {
        web.httpFirewall(new StrictHttpFirewall());
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.NEVER)
                        .sessionFixation().migrateSession()
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .requestMatchers(
                                antMatcher("/"),
                                antMatcher("/login")
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .with(LnurlAuthConfigurer.create(k1Manager, pairingService), it -> {
                });

        return http.build();
    }
}

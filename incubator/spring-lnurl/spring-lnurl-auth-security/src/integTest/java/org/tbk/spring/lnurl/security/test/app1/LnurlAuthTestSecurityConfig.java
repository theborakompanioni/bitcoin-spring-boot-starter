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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.tbk.lnurl.auth.K1Manager;
import org.tbk.lnurl.auth.LnurlAuthPairingService;
import org.tbk.spring.lnurl.security.LnurlAuthConfigurer;

@Slf4j
@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
class LnurlAuthTestSecurityConfig implements WebSecurityCustomizer {

    @NonNull
    private final LnurlAuthPairingService pairingService;

    @NonNull
    private final K1Manager k1Manager;
    @NonNull
    private final UserDetailsService userDetailsService;

    @Override
    public void customize(WebSecurity web) {
        web.httpFirewall(new StrictHttpFirewall());
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .apply(LnurlAuthConfigurer.create(k1Manager, pairingService)
                        .authenticationUserDetailsService(userDetailsService))
                .and()
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.NEVER)
                        .sessionFixation().migrateSession()
                )
                .logout().and()
                .authorizeRequests(authorize -> authorize
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .antMatchers("/").permitAll()
                        .antMatchers("/login").permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}

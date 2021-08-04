package org.tbk.lightning.lnurl.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.tbk.lnurl.auth.LnurlAuthPairingService;
import org.tbk.lnurl.auth.SimpleK1Manager;
import org.tbk.spring.lnurl.security.LnurlAuthConfigurer;

@Slf4j
@EnableWebSecurity
@Configuration
public class LnurlAuthExampleApplicationSecurityConfig extends WebSecurityConfigurerAdapter {
    static final String LNURL_AUTH_WALLET_LOGIN_PATH = "/api/v1/lnauth/login/wallet";
    static final String LNURL_AUTH_SESSION_LOGIN_PATH = "/api/v1/lnauth/login/session";
    static final String LNURL_AUTH_SESSION_K1_KEY = "my_lnurl_auth_k1";

    public static String lnurlAuthWalletLoginPath() {
        return LNURL_AUTH_WALLET_LOGIN_PATH;
    }

    public static String lnurlAuthSessionLoginPath() {
        return LNURL_AUTH_SESSION_LOGIN_PATH;
    }

    public static String lnurlAuthSessionK1Key() {
        return LNURL_AUTH_SESSION_K1_KEY;
    }

    private final LnurlAuthPairingService lnurlAuthPairingService;
    private final UserDetailsService userDetailsService;

    public LnurlAuthExampleApplicationSecurityConfig(LnurlAuthPairingService lnurlAuthPairingService,
                                                     UserDetailsService userDetailsService) {
        this.lnurlAuthPairingService = lnurlAuthPairingService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected UserDetailsService userDetailsService() {
        return userDetailsService;
    }

    @Override
    public void configure(WebSecurity web) {
        web.httpFirewall(new StrictHttpFirewall());
    }
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .userDetailsService(userDetailsService())
                .csrf().disable()
                .cors().disable()
                .apply(new LnurlAuthConfigurer())
                .k1Manager(k1Manager())
                .pairingService(lnurlAuthPairingService)
                .walletLoginUrl(lnurlAuthWalletLoginPath())
                .sessionLoginUrl(lnurlAuthSessionLoginPath())
                .sessionK1Key(lnurlAuthSessionK1Key())
                .and()
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.NEVER)
                        .sessionFixation().migrateSession()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .logoutSuccessUrl("/login?logout")
                )
                .headers(headers -> headers
                        .xssProtection()
                        .xssProtectionEnabled(true)
                        .block(true)
                        .and()
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; script-src 'self'"))
                )
                .authorizeRequests(authorize -> authorize
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .antMatchers("/").permitAll()
                        .antMatchers("/index.html").permitAll()
                        .antMatchers("/authenticated.html").authenticated()
                        // login page should be readable by all - session will be initialized
                        .antMatchers("/login").permitAll()
                        .antMatchers(lnurlAuthWalletLoginPath()).permitAll()
                        .antMatchers(lnurlAuthSessionLoginPath()).permitAll()
                        .antMatchers("/api/v1/lnauth/**").permitAll()
                        .anyRequest().authenticated()
                );
    }

    @Bean
    public SimpleK1Manager k1Manager() {
        return new SimpleK1Manager();
    }

}

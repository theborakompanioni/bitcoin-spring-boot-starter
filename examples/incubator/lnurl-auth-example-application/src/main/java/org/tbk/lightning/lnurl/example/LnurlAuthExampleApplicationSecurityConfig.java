package org.tbk.lightning.lnurl.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.tbk.lightning.lnurl.example.domain.WalletUserService;
import org.tbk.lightning.lnurl.example.lnurl.K1Manager;
import org.tbk.lightning.lnurl.example.lnurl.security.*;
import org.tbk.lightning.lnurl.example.security.MyUserDetailsService;

@EnableWebSecurity
@Configuration
public class LnurlAuthExampleApplicationSecurityConfig extends WebSecurityConfigurerAdapter {
    static final String LNURL_AUTH_WALLET_LOGIN_PATH = "/api/v1/lnauth/login/wallet";
    static final String LNURL_AUTH_SESSION_LOGIN_PATH = "/api/v1/lnauth/login/session";

    public static String lnurlAuthWalletLoginPath() {
        return LNURL_AUTH_WALLET_LOGIN_PATH;
    }

    public static String lnurlAuthSessionLoginPath() {
        return LNURL_AUTH_SESSION_LOGIN_PATH;
    }

    @Autowired
    private K1Manager k1Manager;

    @Autowired
    private WalletUserService walletUserService;

    @Bean
    public MyUserDetailsService userDetailsService() {
        return new MyUserDetailsService(walletUserService);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .cors().disable()
                .sessionManagement(session -> session
                        //.sessionAuthenticationStrategy()
                        //.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionCreationPolicy(SessionCreationPolicy.NEVER)
                        .sessionFixation().none()
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(true)
                )
                .authorizeRequests(authorize -> authorize
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .antMatchers("/").permitAll()
                        .antMatchers("/index.html").permitAll()
                        .antMatchers("/authenticated.html").authenticated()
                        //.antMatchers("/login").anonymous()
                        //.antMatchers("/api/v1/lnauth/**").anonymous()
                        .antMatchers("/login").permitAll()
                        .antMatchers("/api/v1/lnauth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .logout(logout -> logout
                                .invalidateHttpSession(true)
                                //.deleteCookies("SESSION").permitAll()
                                .clearAuthentication(true)
                        //.logoutUrl("/logout")
                        //.logoutSuccessHandler(getLogoutSuccessHandler())
                )
                .headers(headers -> headers
                        .xssProtection()
                        .xssProtectionEnabled(true)
                        .block(true)
                        .and()
                        .contentSecurityPolicy("script-src 'self'")
                )
                .addFilterBefore(lnurlAuthWalletAuthenticationFilter(), AnonymousAuthenticationFilter.class)
                .addFilterAfter(lnurlAuthSessionAuthenticationFilter(), LnurlAuthWalletAuthenticationFilter.class);
    }

    @Bean
    public LnurlAuthWalletAuthenticationFilter lnurlAuthWalletAuthenticationFilter() {
        return new LnurlAuthWalletAuthenticationFilter(lnurlAuthWalletLoginPath(), authenticationManager());
    }

    @Bean
    public LnurlAuthWalletAuthenticationProvider lnurlAuthWalletAuthenticationProvider() {
        return new LnurlAuthWalletAuthenticationProvider(k1Manager, walletUserService, userDetailsService());
    }

    @Bean
    public LnurlAuthSessionMigrateAuthenticationFilter lnurlAuthSessionAuthenticationFilter() {
        return new LnurlAuthSessionMigrateAuthenticationFilter(lnurlAuthSessionLoginPath(), authenticationManager());
    }

    @Bean
    public LnurlAuthSessionAuthenticationProvider lnurlAuthSessionAuthenticationProvider() {
        return new LnurlAuthSessionAuthenticationProvider(walletUserService, userDetailsService());
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(lnurlAuthWalletAuthenticationProvider(), lnurlAuthSessionAuthenticationProvider());
    }
}

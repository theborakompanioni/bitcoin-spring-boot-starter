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
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.tbk.lightning.lnurl.example.domain.WalletUserService;
import org.tbk.lightning.lnurl.example.lnurl.K1Manager;
import org.tbk.lightning.lnurl.example.lnurl.LnurlAuthFactory;
import org.tbk.lightning.lnurl.example.lnurl.security.LnurlAuthenticationFilter;
import org.tbk.lightning.lnurl.example.lnurl.security.LnurlAuthenticationProvider;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@EnableWebSecurity
@Configuration
public class LnurlAuthExampleApplicationSecurityConfig extends WebSecurityConfigurerAdapter {
    static final String LNURL_AUTH_PATH = "/api/v1/lnauth/login";

    public static String lnurlAuthLoginPath() {
        return LNURL_AUTH_PATH;
    }

    @Autowired
    private K1Manager k1Manager;

    @Autowired
    private WalletUserService walletUserService;

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
                .addFilterBefore(lnurlAuthAuthenticationFilter(), AnonymousAuthenticationFilter.class);
    }

    @Bean
    public LnurlAuthenticationFilter lnurlAuthAuthenticationFilter() {
        return new LnurlAuthenticationFilter(lnurlAuthLoginPath(), authenticationManager());
    }

    @Bean
    public LnurlAuthenticationProvider lnurlAuthAuthenticationProvider() {
        return new LnurlAuthenticationProvider(k1Manager, walletUserService);
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(lnurlAuthAuthenticationProvider());
    }
}

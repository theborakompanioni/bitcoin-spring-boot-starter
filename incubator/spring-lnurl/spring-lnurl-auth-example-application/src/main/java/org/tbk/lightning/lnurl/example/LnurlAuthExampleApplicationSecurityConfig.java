package org.tbk.lightning.lnurl.example;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.tbk.lnurl.auth.K1Manager;
import org.tbk.lnurl.auth.LnurlAuthFactory;
import org.tbk.lnurl.auth.LnurlAuthPairingService;
import org.tbk.spring.lnurl.security.LnurlAuthConfigurer;

@Slf4j
@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class LnurlAuthExampleApplicationSecurityConfig extends WebSecurityConfigurerAdapter {
    static final String LNURL_AUTH_LOGIN_PAGE_PATH = "/login";
    static final String LNURL_AUTH_WALLET_LOGIN_PATH = "/api/v1/lnurl-auth/login/wallet";
    static final String LNURL_AUTH_SESSION_LOGIN_PATH = "/api/v1/lnurl-auth/login/session";
    static final String LNURL_AUTH_SESSION_K1_KEY = "my_lnurl_auth_k1";

    public static String lnurlAuthLoginPagePath() {
        return LNURL_AUTH_LOGIN_PAGE_PATH;
    }

    public static String lnurlAuthWalletLoginPath() {
        return LNURL_AUTH_WALLET_LOGIN_PATH;
    }

    public static String lnurlAuthSessionLoginPath() {
        return LNURL_AUTH_SESSION_LOGIN_PATH;
    }

    public static String lnurlAuthSessionK1Key() {
        return LNURL_AUTH_SESSION_K1_KEY;
    }

    @NonNull
    private final K1Manager lnurlAuthk1Manager;

    @NonNull
    private final LnurlAuthPairingService lnurlAuthPairingService;

    @NonNull
    private final UserDetailsService userDetailsService;

    @NonNull
    private final LnurlAuthFactory lnurlAuthFactory;

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
                /*
                 * An overly verbose definition of the lnurl-auth authorization filter configuration.
                 * This should demonstrate the ability of creating more customized setups.
                 */
                .apply(new LnurlAuthConfigurer()
                        .k1Manager(lnurlAuthk1Manager)
                        .pairingService(lnurlAuthPairingService)
                        .lnurlAuthFactory(lnurlAuthFactory)
                        .loginPageEndpoint(login -> login
                                .enable(true)
                                .baseUri(lnurlAuthLoginPagePath())
                        )
                        .sessionEndpoint(session -> session
                                .baseUri(lnurlAuthSessionLoginPath())
                                .sessionK1Key(lnurlAuthSessionK1Key())
                        )
                        .walletEndpoint(wallet -> wallet
                                .baseUri(lnurlAuthWalletLoginPath())
                        )
                )
                .and()
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.NEVER)
                        .sessionFixation().migrateSession()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .logoutSuccessUrl("/")
                )
                .headers(headers -> headers
                        .xssProtection()
                        .xssProtectionEnabled(true)
                        .block(true)
                        .and()
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; "
                                + "script-src 'self'; "
                                + "img-src 'self' data: https://robohash.org"))
                )
                .authorizeRequests(authorize -> authorize
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .antMatchers("/").permitAll()
                        .antMatchers("/index.html").permitAll()
                        .antMatchers("/authenticated.html").authenticated()
                        .antMatchers("/api/v1/demo/**").permitAll()
                        .anyRequest().authenticated()
                );
    }

}

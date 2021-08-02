package org.tbk.spring.lnurl.security;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.util.Assert;
import org.tbk.lnurl.auth.K1Manager;
import org.tbk.lnurl.auth.LnurlAuthPairingService;
import org.tbk.spring.lnurl.security.session.LnurlAuthSessionAuthenticationFilter;
import org.tbk.spring.lnurl.security.session.LnurlAuthSessionAuthenticationProvider;
import org.tbk.spring.lnurl.security.wallet.LnurlAuthWalletAuthenticationFilter;
import org.tbk.spring.lnurl.security.wallet.LnurlAuthWalletAuthenticationProvider;

@RequiredArgsConstructor
public class LnurlAuthConfigurer extends AbstractHttpConfigurer<LnurlAuthConfigurer, HttpSecurity> {
    private static final String DEFAULT_WALLET_LOGIN_URL = "/lnurl-auth/wallet/login";
    private static final String DEFAULT_SESSION_LOGIN_URL = "/lnurl-auth/session/migrate";
    private static final String DEFAULT_SESSION_K1_KEY = "LNURL_AUTH_K1";

    public static String defaultWalletLoginUrl() {
        return DEFAULT_WALLET_LOGIN_URL;
    }

    public static String defaultSessionLoginUrl() {
        return DEFAULT_SESSION_LOGIN_URL;
    }

    public static String defaultSessionK1Key() {
        return DEFAULT_SESSION_K1_KEY;
    }

    @NonNull
    private final K1Manager k1Manager;

    @NonNull
    private final LnurlAuthPairingService lnurlAuthPairingService;

    protected String walletLoginUrl = defaultWalletLoginUrl();
    protected String sessionLoginUrl = defaultSessionLoginUrl();
    protected String sessionK1Key = defaultSessionK1Key();

    public LnurlAuthConfigurer walletLoginUrl(String walletLoginUrl) {
        Assert.hasText(walletLoginUrl, "walletLoginUrl cannot be empty");
        this.walletLoginUrl = walletLoginUrl;
        return this;
    }

    public LnurlAuthConfigurer sessionLoginUrl(String sessionLoginUrl) {
        Assert.hasText(sessionLoginUrl, "sessionLoginUrl cannot be empty");
        this.sessionLoginUrl = sessionLoginUrl;
        return this;
    }

    public LnurlAuthConfigurer sessionK1Key(String sessionK1Key) {
        Assert.hasText(sessionK1Key, "sessionK1Key cannot be empty");
        this.sessionK1Key = sessionK1Key;
        return this;
    }

    @Override
    public void configure(HttpSecurity http) {
        AuthenticationManager authenticationManager = http.getSharedObject(AuthenticationManager.class);
        SessionAuthenticationStrategy sessionAuthenticationStrategy = http.getSharedObject(SessionAuthenticationStrategy.class);
        UserDetailsService userDetailsService = http.getSharedObject(UserDetailsService.class);

        LnurlAuthWalletAuthenticationFilter walletAuthFilter = new LnurlAuthWalletAuthenticationFilter(walletLoginUrl);
        walletAuthFilter.setAuthenticationManager(authenticationManager);
        walletAuthFilter.setSessionAuthenticationStrategy(new NullAuthenticatedSessionStrategy());

        LnurlAuthSessionAuthenticationFilter sessionAuthFilter = new LnurlAuthSessionAuthenticationFilter(sessionLoginUrl, sessionK1Key);
        sessionAuthFilter.setAuthenticationManager(authenticationManager);
        sessionAuthFilter.setSessionAuthenticationStrategy(sessionAuthenticationStrategy);

        http
                .authenticationProvider(postProcess(walletAuthenticationProvider(userDetailsService)))
                .authenticationProvider(postProcess(sessionAuthenticationProvider(userDetailsService)))
                .addFilterBefore(postProcess(walletAuthFilter), AnonymousAuthenticationFilter.class)
                .addFilterBefore(postProcess(sessionAuthFilter), AnonymousAuthenticationFilter.class);
    }

    protected LnurlAuthWalletAuthenticationProvider walletAuthenticationProvider(UserDetailsService userDetailsService) {
        return new LnurlAuthWalletAuthenticationProvider(k1Manager, lnurlAuthPairingService, userDetailsService);
    }

    protected LnurlAuthSessionAuthenticationProvider sessionAuthenticationProvider(UserDetailsService userDetailsService) {
        return new LnurlAuthSessionAuthenticationProvider(lnurlAuthPairingService, userDetailsService);
    }
}

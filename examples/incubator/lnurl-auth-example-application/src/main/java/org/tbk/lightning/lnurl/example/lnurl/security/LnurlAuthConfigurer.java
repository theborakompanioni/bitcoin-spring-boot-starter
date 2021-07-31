package org.tbk.lightning.lnurl.example.lnurl.security;

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
import org.tbk.lightning.lnurl.example.lnurl.K1Manager;
import org.tbk.lightning.lnurl.example.lnurl.security.session.LnurlAuthSessionAuthenticationFilter;
import org.tbk.lightning.lnurl.example.lnurl.security.session.LnurlAuthSessionAuthenticationProvider;
import org.tbk.lightning.lnurl.example.lnurl.security.wallet.LnurlAuthWalletAuthenticationFilter;
import org.tbk.lightning.lnurl.example.lnurl.security.wallet.LnurlAuthWalletAuthenticationProvider;

@RequiredArgsConstructor
public class LnurlAuthConfigurer extends AbstractHttpConfigurer<LnurlAuthConfigurer, HttpSecurity> {
    private static final String DEFAULT_WALLET_LOGIN_URL = "/lnurl-auth/wallet/login";
    private static final String DEFAUT_SESSION_LOGIN_URL = "/lnurl-auth/session/migrate";

    public static String defaultWalletLoginUrl() {
        return DEFAULT_WALLET_LOGIN_URL;
    }

    public static String defaultSessionLoginUrl() {
        return DEFAUT_SESSION_LOGIN_URL;
    }

    @NonNull
    private final K1Manager k1Manager;

    @NonNull
    private final LnurlAuthSecurityService lnurlAuthSecurityService;

    protected String walletLoginUrl = defaultWalletLoginUrl();
    protected String sessionLoginUrl = defaultSessionLoginUrl();

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

    @Override
    public void configure(HttpSecurity http) {
        AuthenticationManager authenticationManager = http.getSharedObject(AuthenticationManager.class);
        SessionAuthenticationStrategy sessionAuthenticationStrategy = http.getSharedObject(SessionAuthenticationStrategy.class);
        UserDetailsService userDetailsService = http.getSharedObject(UserDetailsService.class);

        LnurlAuthWalletAuthenticationFilter walletAuthFilter = new LnurlAuthWalletAuthenticationFilter(walletLoginUrl);
        walletAuthFilter.setAuthenticationManager(authenticationManager);
        walletAuthFilter.setSessionAuthenticationStrategy(new NullAuthenticatedSessionStrategy());

        LnurlAuthSessionAuthenticationFilter sessionAuthFilter = new LnurlAuthSessionAuthenticationFilter(sessionLoginUrl);
        sessionAuthFilter.setAuthenticationManager(authenticationManager);
        sessionAuthFilter.setSessionAuthenticationStrategy(sessionAuthenticationStrategy);

        http
                .authenticationProvider(postProcess(walletAuthenticationProvider(userDetailsService)))
                .authenticationProvider(postProcess(sessionAuthenticationProvider(userDetailsService)))
                .addFilterBefore(postProcess(walletAuthFilter), AnonymousAuthenticationFilter.class)
                .addFilterBefore(postProcess(sessionAuthFilter), AnonymousAuthenticationFilter.class);
    }

    protected LnurlAuthWalletAuthenticationProvider walletAuthenticationProvider(UserDetailsService userDetailsService) {
        return new LnurlAuthWalletAuthenticationProvider(k1Manager, lnurlAuthSecurityService, userDetailsService);
    }

    protected LnurlAuthSessionAuthenticationProvider sessionAuthenticationProvider(UserDetailsService userDetailsService) {
        return new LnurlAuthSessionAuthenticationProvider(lnurlAuthSecurityService, userDetailsService);
    }
}

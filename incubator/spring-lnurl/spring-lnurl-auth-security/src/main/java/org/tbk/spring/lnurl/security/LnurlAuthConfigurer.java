package org.tbk.spring.lnurl.security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.util.Assert;
import org.tbk.lnurl.auth.K1Manager;
import org.tbk.lnurl.auth.LnurlAuthFactory;
import org.tbk.lnurl.auth.LnurlAuthPairingService;
import org.tbk.spring.lnurl.security.session.LnurlAuthSessionAuthenticationFilter;
import org.tbk.spring.lnurl.security.session.LnurlAuthSessionAuthenticationProvider;
import org.tbk.spring.lnurl.security.ui.LnurlAuthLoginPageGeneratingFilter;
import org.tbk.spring.lnurl.security.wallet.LnurlAuthWalletAuthenticationFilter;
import org.tbk.spring.lnurl.security.wallet.LnurlAuthWalletAuthenticationProvider;

import static java.util.Objects.requireNonNull;

public class LnurlAuthConfigurer extends AbstractHttpConfigurer<LnurlAuthConfigurer, HttpSecurity> {
    private static final String DEFAULT_LOGIN_PAGE_URL = "/lnurl-auth/login";
    private static final String DEFAULT_WALLET_LOGIN_URL = "/lnurl-auth/wallet/login";
    private static final String DEFAULT_SESSION_LOGIN_URL = "/lnurl-auth/session/migrate";
    private static final String DEFAULT_SESSION_K1_KEY = "LNURL_AUTH_K1";

    public static String defaultLoginPageUrl() {
        return DEFAULT_LOGIN_PAGE_URL;
    }

    public static String defaultWalletLoginUrl() {
        return DEFAULT_WALLET_LOGIN_URL;
    }

    public static String defaultSessionLoginUrl() {
        return DEFAULT_SESSION_LOGIN_URL;
    }

    public static String defaultSessionK1Key() {
        return DEFAULT_SESSION_K1_KEY;
    }

    protected K1Manager k1Manager;

    protected LnurlAuthPairingService pairingService;

    protected LnurlAuthFactory lnurlAuthFactory;

    protected String loginPageUrl = defaultLoginPageUrl();
    protected String walletLoginUrl = defaultWalletLoginUrl();
    protected String sessionLoginUrl = defaultSessionLoginUrl();
    protected String sessionK1Key = defaultSessionK1Key();

    protected boolean defaultLoginPageEnabled = true;

    @Override
    public void init(HttpSecurity http) {
        // http.setSharedObject(DefaultLnurlAuthLoginPageGeneratingFilter.class, this.loginPageGeneratingFilter);
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
                .addFilterBefore(postProcess(walletAuthFilter), AnonymousAuthenticationFilter.class);

        http
                .authenticationProvider(postProcess(sessionAuthenticationProvider(userDetailsService)))
                .addFilterBefore(postProcess(sessionAuthFilter), AnonymousAuthenticationFilter.class);

        if (defaultLoginPageEnabled) {
            if (lnurlAuthFactory == null) {
                String errorMessage = "Cannot create default login page when 'lnurlAuthFactory' is null. "
                        + "Consider adding the necessary bean or disable default login page generation. ";
                throw new IllegalStateException(errorMessage);
            }

            LnurlAuthLoginPageGeneratingFilter loginPageGeneratingFilter =
                    new LnurlAuthLoginPageGeneratingFilter(lnurlAuthFactory, sessionK1Key, loginPageUrl, sessionLoginUrl);
            http.addFilterBefore(postProcess(loginPageGeneratingFilter), AnonymousAuthenticationFilter.class);
        }
    }

    public LnurlAuthConfigurer k1Manager(K1Manager k1Manager) {
        this.k1Manager = requireNonNull(k1Manager);
        return this;
    }

    public LnurlAuthConfigurer pairingService(LnurlAuthPairingService pairingService) {
        this.pairingService = requireNonNull(pairingService);
        return this;
    }

    public LnurlAuthConfigurer lnurlAuthFactory(LnurlAuthFactory lnurlAuthFactory) {
        this.lnurlAuthFactory = requireNonNull(lnurlAuthFactory);
        return this;
    }

    public LnurlAuthConfigurer walletLoginUrl(String walletLoginUrl) {
        Assert.hasText(walletLoginUrl, "walletLoginUrl cannot be empty");
        this.walletLoginUrl = walletLoginUrl;
        return this;
    }

    public LnurlAuthConfigurer loginPageUrl(String loginPageUrl) {
        Assert.hasText(loginPageUrl, "loginPageUrl cannot be empty");
        this.loginPageUrl = loginPageUrl;
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

    public LnurlAuthConfigurer disableDefaultLoginPage() {
        return enableDefaultLoginPage(false);
    }

    public LnurlAuthConfigurer enableDefaultLoginPage() {
        return enableDefaultLoginPage(true);
    }

    public LnurlAuthConfigurer enableDefaultLoginPage(boolean defaultLoginPageEnabled) {
        this.defaultLoginPageEnabled = defaultLoginPageEnabled;
        return this;
    }

    protected LnurlAuthWalletAuthenticationProvider walletAuthenticationProvider(UserDetailsService userDetailsService) {
        return new LnurlAuthWalletAuthenticationProvider(k1Manager, pairingService, userDetailsService);
    }

    protected LnurlAuthSessionAuthenticationProvider sessionAuthenticationProvider(UserDetailsService userDetailsService) {
        return new LnurlAuthSessionAuthenticationProvider(pairingService, userDetailsService);
    }
}

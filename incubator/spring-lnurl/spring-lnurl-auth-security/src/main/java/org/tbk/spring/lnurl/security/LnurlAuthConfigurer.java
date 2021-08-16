package org.tbk.spring.lnurl.security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.SecurityConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;
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

    /**
     * Creates a new instance with default login page disabled.
     * You can enable it by providing an {@link LnurlAuthFactory} later,
     * or by using factory method {@link #createWithDefaultLoginPage}.
     *
     * @return an new instance of {@link LnurlAuthConfigurer} with default login page disabled
     */
    public static LnurlAuthConfigurer create(K1Manager k1Manager, LnurlAuthPairingService pairingService) {
        return new LnurlAuthConfigurer()
                .k1Manager(k1Manager)
                .pairingService(pairingService)
                .loginPageEndpoint(LoginPageEndpointConfig::disable);
    }

    /**
     * Creates a new instance with default login page enabled.
     *
     * @return an new instance of {@link LnurlAuthConfigurer} with default login page enabled
     */
    public static LnurlAuthConfigurer createWithDefaultLoginPage(K1Manager k1Manager, LnurlAuthPairingService pairingService, LnurlAuthFactory lnurlAuthFactory) {
        return create(k1Manager, pairingService)
                .lnurlAuthFactory(lnurlAuthFactory)
                .loginPageEndpoint(LoginPageEndpointConfig::enable);
    }

    private final SessionEndpointConfig sessionEndpointConfig = new SessionEndpointConfig();
    private final WalletEndpointConfig walletEndpointConfig = new WalletEndpointConfig();
    private final LoginPageEndpointConfig loginPageEndpointConfig = new LoginPageEndpointConfig();

    private K1Manager k1Manager;

    private LnurlAuthPairingService pairingService;

    private LnurlAuthFactory lnurlAuthFactory;

    /**
     * Creates a new instance
     * <p>
     * Most of springs internal {@link SecurityConfigurer} have default constructors,
     * so we try to provide a similar approach - even when some attributes are effectively non-nullable
     * like {@link #k1Manager} and {@link #pairingService}.
     *
     * @see org.springframework.security.config.annotation.web.configurers.HeadersConfigurer#HeadersConfigurer()
     */
    public LnurlAuthConfigurer() {
    }

    /**
     * Add the {@link K1Manager} that is used to verify `k1` values in authorization attempts.
     *
     * @param k1Manager the {@link K1Manager}
     * @return the {@link LnurlAuthConfigurer} for further customizations
     */
    public LnurlAuthConfigurer k1Manager(K1Manager k1Manager) {
        this.k1Manager = requireNonNull(k1Manager);
        return this;
    }

    /**
     * Add the {@link LnurlAuthPairingService} that is used to link session and wallet authorization
     * mechanisms. This service is used to pair a users browser session with a previously made wallet
     * authentication request.
     *
     * @param pairingService the {@link LnurlAuthPairingService}
     * @return the {@link LnurlAuthConfigurer} for further customizations
     */
    public LnurlAuthConfigurer pairingService(LnurlAuthPairingService pairingService) {
        this.pairingService = requireNonNull(pairingService);
        return this;
    }

    /**
     * Add the {@link LnurlAuthFactory} that is used create lnurl-auth challenges.
     * If a non-null value is provided, the default login page will be enabled.
     *
     * @param lnurlAuthFactory the {@link LnurlAuthFactory}
     * @return the {@link LnurlAuthConfigurer} for further customizations
     */
    public LnurlAuthConfigurer lnurlAuthFactory(LnurlAuthFactory lnurlAuthFactory) {
        this.lnurlAuthFactory = lnurlAuthFactory;
        if (lnurlAuthFactory == null) {
            this.loginPageEndpointConfig.disable();
        }
        return this;
    }

    @Override
    public void init(HttpSecurity http) {
        if (k1Manager == null) {
            String errorMessage = "Cannot create lnurl-auth authentication handling when 'k1Manager' is null. "
                    + "Please add the necessary bean or disable lnurl-auth authentication.";
            throw new IllegalStateException(errorMessage);
        }

        if (pairingService == null) {
            String errorMessage = "Cannot create lnurl-auth authentication handling when 'pairingService' is null. "
                    + "Please add the necessary bean or disable lnurl-auth authentication.";
            throw new IllegalStateException(errorMessage);
        }

        if (loginPageEndpointConfig.enabled) {
            if (lnurlAuthFactory == null) {
                String errorMessage = "Cannot create default lnurl-auth login page when 'lnurlAuthFactory' is null. "
                        + "Consider adding the necessary bean or disable default login page generation.";
                throw new IllegalStateException(errorMessage);
            }
        }
    }

    @Override
    public void configure(HttpSecurity http) {
        AuthenticationManager authenticationManager = http.getSharedObject(AuthenticationManager.class);
        SessionAuthenticationStrategy sessionAuthenticationStrategy = http.getSharedObject(SessionAuthenticationStrategy.class);
        UserDetailsService userDetailsService = http.getSharedObject(UserDetailsService.class);

        LnurlAuthWalletAuthenticationFilter walletAuthFilter = new LnurlAuthWalletAuthenticationFilter(walletEndpointConfig.authorizationRequestBaseUri);
        walletAuthFilter.setAuthenticationManager(authenticationManager);

        LnurlAuthSessionAuthenticationFilter sessionAuthFilter = new LnurlAuthSessionAuthenticationFilter(sessionEndpointConfig.authorizationRequestBaseUri, sessionEndpointConfig.sessionK1Key);
        sessionAuthFilter.setAuthenticationManager(authenticationManager);
        sessionAuthFilter.setSessionAuthenticationStrategy(sessionAuthenticationStrategy);

        http
                .authenticationProvider(postProcess(walletAuthenticationProvider(userDetailsService)))
                .addFilterAfter(postProcess(walletAuthFilter), SecurityContextHolderAwareRequestFilter.class);

        http
                .authenticationProvider(postProcess(sessionAuthenticationProvider(userDetailsService)))
                .addFilterAfter(postProcess(sessionAuthFilter), SecurityContextHolderAwareRequestFilter.class);

        if (loginPageEndpointConfig.enabled) {
            requireNonNull(lnurlAuthFactory, "Sanity check: 'lnurlAuthFactory' must not be null");

            LnurlAuthLoginPageGeneratingFilter loginPageGeneratingFilter = new LnurlAuthLoginPageGeneratingFilter(
                    lnurlAuthFactory, loginPageEndpointConfig.authorizationRequestBaseUri,
                    sessionEndpointConfig.authorizationRequestBaseUri, sessionEndpointConfig.sessionK1Key);
            http.addFilterAfter(postProcess(loginPageGeneratingFilter), SecurityContextHolderAwareRequestFilter.class);
        }
    }

    protected LnurlAuthWalletAuthenticationProvider walletAuthenticationProvider(UserDetailsService userDetailsService) {
        return new LnurlAuthWalletAuthenticationProvider(k1Manager, pairingService, userDetailsService);
    }

    protected LnurlAuthSessionAuthenticationProvider sessionAuthenticationProvider(UserDetailsService userDetailsService) {
        return new LnurlAuthSessionAuthenticationProvider(pairingService, userDetailsService);
    }

    /**
     * Returns the {@link LnurlAuthConfigurer.WalletEndpointConfig} for configuring the LNURL Authorization Server's
     * Wallet Endpoint.
     *
     * @return the {@link LnurlAuthConfigurer.WalletEndpointConfig}
     */
    public LnurlAuthConfigurer.WalletEndpointConfig walletEndpoint() {
        return this.walletEndpointConfig;
    }

    /**
     * Configures the LNURL Authorization Server's Wallet Endpoint.
     *
     * @param walletEndpointConfigCustomizer the {@link Customizer} to provide more options
     *                                       for the {@link LnurlAuthConfigurer.WalletEndpointConfig}
     * @return the {@link LnurlAuthConfigurer} for further customizations
     */
    public LnurlAuthConfigurer walletEndpoint(Customizer<WalletEndpointConfig> walletEndpointConfigCustomizer) {
        walletEndpointConfigCustomizer.customize(this.walletEndpointConfig);
        return this;
    }

    public final class WalletEndpointConfig {

        private String authorizationRequestBaseUri = defaultWalletLoginUrl();

        /**
         * Sets the base {@code URI} used for wallet authorization requests.
         *
         * @param authorizationRequestBaseUri the base {@code URI} used for wallet authorization
         *                                    requests
         * @return the {@link LnurlAuthConfigurer.WalletEndpointConfig} for further configuration
         */
        public WalletEndpointConfig baseUri(String authorizationRequestBaseUri) {
            Assert.hasText(authorizationRequestBaseUri, "authorizationRequestBaseUri cannot be empty");
            this.authorizationRequestBaseUri = authorizationRequestBaseUri;
            return this;
        }

        /**
         * Returns the {@link LnurlAuthConfigurer} for further configuration.
         *
         * @return the {@link LnurlAuthConfigurer}
         */
        public LnurlAuthConfigurer and() {
            return LnurlAuthConfigurer.this;
        }
    }

    /**
     * Returns the {@link LnurlAuthConfigurer.SessionEndpointConfig} for configuring the LNURL Authorization Server's
     * Session Endpoint.
     *
     * @return the {@link LnurlAuthConfigurer.SessionEndpointConfig}
     */
    public LnurlAuthConfigurer.SessionEndpointConfig sessionEndpoint() {
        return this.sessionEndpointConfig;
    }

    /**
     * Configures the LNURL Authorization Server's Session Endpoint.
     *
     * @param sessionEndpointCustomizer the {@link Customizer} to provide more options
     *                                  for the {@link LnurlAuthConfigurer.SessionEndpointConfig}
     * @return the {@link LnurlAuthConfigurer} for further customizations
     */
    public LnurlAuthConfigurer sessionEndpoint(Customizer<SessionEndpointConfig> sessionEndpointCustomizer) {
        sessionEndpointCustomizer.customize(this.sessionEndpointConfig);
        return this;
    }

    public final class SessionEndpointConfig {

        private String authorizationRequestBaseUri = defaultSessionLoginUrl();

        private String sessionK1Key = defaultSessionK1Key();

        /**
         * Sets the base {@code URI} used for session authorization requests.
         *
         * @param authorizationRequestBaseUri the base {@code URI} used for session authorization
         *                                    requests
         * @return the {@link LnurlAuthConfigurer.SessionEndpointConfig} for further configuration
         */
        public SessionEndpointConfig baseUri(String authorizationRequestBaseUri) {
            Assert.hasText(authorizationRequestBaseUri, "authorizationRequestBaseUri cannot be empty");
            this.authorizationRequestBaseUri = authorizationRequestBaseUri;
            return this;
        }

        /**
         * Sets the name of the property in the session used as LNURL `k1` value.
         *
         * @param sessionK1Key the name of the key to find `k1` in the user session
         * @return the {@link LnurlAuthConfigurer.SessionEndpointConfig} for further configuration
         */
        public SessionEndpointConfig sessionK1Key(String sessionK1Key) {
            Assert.hasText(sessionK1Key, "sessionK1Key cannot be empty");
            this.sessionK1Key = sessionK1Key;
            return this;
        }

        /**
         * Returns the {@link LnurlAuthConfigurer} for further configuration.
         *
         * @return the {@link LnurlAuthConfigurer}
         */
        public LnurlAuthConfigurer and() {
            return LnurlAuthConfigurer.this;
        }
    }


    /**
     * Returns the {@link LnurlAuthConfigurer.LoginPageEndpointConfig} for configuring the LNURL Authorization Server's
     * Login Endpoint.
     *
     * @return the {@link LnurlAuthConfigurer.LoginPageEndpointConfig}
     */
    public LnurlAuthConfigurer.LoginPageEndpointConfig loginPageEndpoint() {
        return this.loginPageEndpointConfig;
    }

    /**
     * Configures the LNURL Authorization Server's Login Page Endpoint.
     *
     * @param loginPageEndpointConfigCustomizer the {@link Customizer} to provide more options
     *                                          for the {@link LnurlAuthConfigurer.LoginPageEndpointConfig}
     * @return the {@link LnurlAuthConfigurer} for further customizations
     */
    public LnurlAuthConfigurer loginPageEndpoint(Customizer<LoginPageEndpointConfig> loginPageEndpointConfigCustomizer) {
        loginPageEndpointConfigCustomizer.customize(this.loginPageEndpointConfig);
        return this;
    }

    public final class LoginPageEndpointConfig {

        private String authorizationRequestBaseUri = defaultLoginPageUrl();

        private boolean enabled = false;

        /**
         * Sets the base {@code URI} used for login page requests.
         *
         * @param authorizationRequestBaseUri the base {@code URI} used for login page requests
         * @return the {@link LnurlAuthConfigurer.LoginPageEndpointConfig} for further configuration
         */
        public LoginPageEndpointConfig baseUri(String authorizationRequestBaseUri) {
            Assert.hasText(authorizationRequestBaseUri, "authorizationRequestBaseUri cannot be empty");
            this.authorizationRequestBaseUri = authorizationRequestBaseUri;
            return this;
        }

        public LoginPageEndpointConfig disable() {
            return enable(false);
        }

        public LoginPageEndpointConfig enable() {
            return enable(true);
        }

        public LoginPageEndpointConfig enable(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Returns the {@link LnurlAuthConfigurer} for further configuration.
         *
         * @return the {@link LnurlAuthConfigurer}
         */
        public LnurlAuthConfigurer and() {
            return LnurlAuthConfigurer.this;
        }
    }
}

package org.tbk.lightning.lnurl.example;

import jakarta.servlet.ServletContext;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.berndpruenster.netlayer.tor.Tor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.tbk.lightning.lnurl.example.domain.WalletUserService;
import org.tbk.lightning.lnurl.example.security.LnurlAuthUserPairingServiceImpl;
import org.tbk.lightning.lnurl.example.security.UserDetailsServiceImpl;
import org.tbk.lnurl.auth.K1Manager;
import org.tbk.lnurl.auth.LnurlAuth;
import org.tbk.lnurl.auth.LnurlAuthFactory;
import org.tbk.lnurl.auth.SimpleLnurlAuthFactory;
import org.tbk.spring.lnurl.security.userdetails.LnurlAuthUserPairingService;
import org.tbk.tor.hs.HiddenServiceDefinition;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static org.tbk.lightning.lnurl.example.LnurlAuthExampleApplicationSecurityConfig.lnurlAuthLoginPagePath;
import static org.tbk.lightning.lnurl.example.LnurlAuthExampleApplicationSecurityConfig.lnurlAuthWalletLoginPath;

@Slf4j
@Configuration(proxyBeanMethods = false)
@EnableScheduling
@EnableConfigurationProperties(LnurlAuthExampleApplicationProperties.class)
class LnurlAuthExampleApplicationConfig {

    private final LnurlAuthExampleApplicationProperties properties;

    public LnurlAuthExampleApplicationConfig(LnurlAuthExampleApplicationProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Bean
    @SneakyThrows(URISyntaxException.class)
    LnurlAuthFactory lnurlAuthFactory(K1Manager k1Manager,
                                      ServletContext servletContext,
                                      Optional<Tor> tor, // injects only after tor is running!
                                      Optional<HiddenServiceDefinition> applicationHiddenServiceDefinition) {
        String callbackBaseUrl = properties.getLnurlAuthBaseUrl()
                .or(() -> applicationHiddenServiceDefinition.flatMap(this::buildOnionUrl))
                .orElseThrow(() -> {
                    String errorMessage = "Cannot build lnurl-auth callback base url. Please enable tor or provide an `app.lnurl-auth-base-url` property.";
                    return new IllegalStateException(errorMessage);
                });

        URI callbackUrl = new URIBuilder(callbackBaseUrl)
                .setPath(servletContext.getContextPath() + lnurlAuthWalletLoginPath())
                .build();

        return new SimpleLnurlAuthFactory(callbackUrl, k1Manager);
    }

    @Bean
    UserDetailsService userDetailsService(WalletUserService walletUserService) {
        return new UserDetailsServiceImpl(walletUserService);
    }

    @Bean
    LnurlAuthUserPairingService lnurlAuthUserPairingService(WalletUserService walletUserService) {
        return new LnurlAuthUserPairingServiceImpl(walletUserService);
    }

    @Bean
    @Profile("!test")
    ApplicationRunner lnurlAuthExampleConsoleInfoRunner(LnurlAuthFactory lnurlAuthFactory) {
        return args -> {
            LnurlAuth lnurlAuth = lnurlAuthFactory.createLnUrlAuth();

            log.info("===== LNURL_AUTH ================================");
            log.info("example lnurl-auth: {}", lnurlAuth.toLnurl().toLnurlString());
            log.info("({})", lnurlAuth.toLnurl().toUri());
            log.info("=================================================");
        };
    }

    @Bean
    @Profile("!test")
    ApplicationRunner applicationHiddenServiceConsoleInfoRunner(ServletContext servletContext,
                                                                Optional<Tor> tor, // injects only after tor is running!
                                                                Optional<HiddenServiceDefinition> applicationHiddenServiceDefinition) {
        return args -> {
            if (applicationHiddenServiceDefinition.isEmpty()) {
                log.info("===== TOR IS DISABLED ===========================");
            } else {
                String onionUrl = applicationHiddenServiceDefinition.flatMap(this::buildOnionUrl).orElseThrow();

                String loginUrl = onionUrl + servletContext.getContextPath() + lnurlAuthLoginPagePath();
                log.info("===== TOR IS ENABLED ============================");
                log.info("onion login: {}", loginUrl);
                log.info("=================================================");
            }
        };
    }

    private Optional<String> buildOnionUrl(HiddenServiceDefinition applicationHiddenServiceDefinition) {
        return applicationHiddenServiceDefinition.getVirtualHost()
                .map(virtualHost -> {
                    int port = applicationHiddenServiceDefinition.getVirtualPort();
                    if (port == 80) {
                        return "http://%s".formatted(virtualHost);
                    } else if (port == 443) {
                        return "https://%s".formatted(virtualHost);
                    }
                    return "http://%s:%d".formatted(virtualHost, port);
                });
    }
}

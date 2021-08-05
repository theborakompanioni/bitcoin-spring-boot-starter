package org.tbk.lightning.lnurl.example;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.tbk.lightning.lnurl.example.domain.WalletUserService;
import org.tbk.lightning.lnurl.example.security.LnurlAuthPairingServiceImpl;
import org.tbk.lightning.lnurl.example.security.UserDetailsServiceImpl;
import org.tbk.lnurl.auth.*;
import org.tbk.tor.hs.HiddenServiceDefinition;

import javax.servlet.ServletContext;
import java.net.URI;
import java.net.URISyntaxException;

import static java.util.Objects.requireNonNull;
import static org.tbk.lightning.lnurl.example.LnurlAuthExampleApplicationSecurityConfig.lnurlAuthWalletLoginPath;

@Slf4j
@Configuration(proxyBeanMethods = false)
@EnableScheduling
@EnableConfigurationProperties(LnurlAuthExampleApplicationProperties.class)
public class LnurlAuthExampleApplicationConfig {

    private final LnurlAuthExampleApplicationProperties properties;

    public LnurlAuthExampleApplicationConfig(LnurlAuthExampleApplicationProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Bean
    @SneakyThrows(URISyntaxException.class)
    public LnurlAuthFactory lnurlAuthFactory(ServletContext servletContext, K1Manager k1Manager) {
        URI callbackUrl = new URIBuilder(properties.getLnurlAuthBaseUrl())
                .setPath(servletContext.getContextPath() + lnurlAuthWalletLoginPath())
                .build();

        return new SimpleLnurlAuthFactory(callbackUrl, k1Manager);
    }

    @Bean
    public SimpleK1Manager k1Manager() {
        return new SimpleK1Manager();
    }

    @Bean
    public UserDetailsService userDetailsService(WalletUserService walletUserService) {
        return new UserDetailsServiceImpl(walletUserService);
    }

    @Bean
    public LnurlAuthPairingService lnurlAuthSecurityService(WalletUserService walletUserService) {
        return new LnurlAuthPairingServiceImpl(walletUserService);
    }

    @Bean
    @Profile("!test")
    public ApplicationRunner lnurlAuthExampleConsoleInfoRunner(LnurlAuthFactory lnurlAuthFactory) {
        return args -> {
            LnurlAuth lnUrlAuth = lnurlAuthFactory.createLnUrlAuth();

            log.info("=================================================");
            log.info("===== LNURL_AUTH ================================");
            log.info("example auth url: {}", lnUrlAuth.toUri().toString());
            log.info("=================================================");
        };
    }

    @Bean
    @Profile("!test")
    public ApplicationRunner applicationHiddenServiceConsoleInfoRunner(ServletContext servletContext,
                                                                       HiddenServiceDefinition applicationHiddenServiceDefinition) {
        return args -> {
            String onionUrl = applicationHiddenServiceDefinition.getVirtualHost()
                    .map(val -> {
                        int port = applicationHiddenServiceDefinition.getVirtualPort();
                        if (port == 80) {
                            return "http://" + val;
                        } else if (port == 443) {
                            return "https://" + val;
                        }
                        return "http://" + val + ":" + applicationHiddenServiceDefinition.getVirtualPort();
                    })
                    .map(it -> it + servletContext.getContextPath())
                    .orElseThrow();

            log.info("=================================================");
            log.info("===== TOR IS ENABLED ============================");
            log.info("onion url: {}", onionUrl);
            log.info("=================================================");
        };
    }

}

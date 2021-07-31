package org.tbk.lightning.lnurl.example;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.tbk.lightning.lnurl.example.api.LnUrlAuthLoginApi;
import org.tbk.lightning.lnurl.example.lnurl.K1Manager;
import org.tbk.lightning.lnurl.example.lnurl.LnurlAuthFactory;
import org.tbk.lightning.lnurl.example.lnurl.SimpleLnAuthFactory;
import org.tbk.lnurl.LnUrlAuth;
import org.tbk.tor.hs.HiddenServiceDefinition;

import java.net.URI;
import java.net.URISyntaxException;

import static java.util.Objects.requireNonNull;

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
    public LnurlAuthFactory lnurlAuthFactory(K1Manager k1Manager) {
        URI callbackUrl = new URIBuilder(properties.getLnurlAuthBaseUrl())
                .setPath(LnurlAuthExampleApplicationSecurityConfig.lnurlAuthLoginPath())
                .build();

        return new SimpleLnAuthFactory(callbackUrl, k1Manager);
    }

    @Bean
    @Profile("!test")
    public ApplicationRunner lnurlAuthExampleConsoleInfoRunner(LnurlAuthFactory lnurlAuthFactory) {
        return args -> {
            LnUrlAuth lnUrlAuth = lnurlAuthFactory.createLnUrlAuth();

            log.info("=================================================");
            log.info("===== LNURL_AUTH ================================");
            log.info("login page: {}", new URIBuilder(lnUrlAuth.toUri()).setPath("/login").removeQuery().build());
            log.info("=================================================");
            log.info("example auth url: {}", lnUrlAuth.toUri().toString());
            log.info("=================================================");
        };
    }

    @Bean
    @Profile("!test")
    @ConditionalOnBean(HiddenServiceDefinition.class)
    public ApplicationRunner applicationHiddenServiceConsoleInfoRunner(HiddenServiceDefinition applicationHiddenServiceDefinition) {
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
                    }).orElseThrow();

            log.info("=================================================");
            log.info("===== TOR IS ENABLED ============================");
            log.info("onion url: {}", onionUrl);
            log.info("=================================================");
        };
    }

}

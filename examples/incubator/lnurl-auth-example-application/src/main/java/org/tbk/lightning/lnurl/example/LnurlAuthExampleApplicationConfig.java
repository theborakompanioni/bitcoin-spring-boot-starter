package org.tbk.lightning.lnurl.example;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.tbk.lightning.lnurl.example.lnurl.LnAuthService;
import org.tbk.lnurl.LnUrlAuth;
import org.tbk.tor.hs.HiddenServiceDefinition;

import static java.util.Objects.requireNonNull;

@Slf4j
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(LnurlAuthExampleApplicationProperties.class)
public class LnurlAuthExampleApplicationConfig {

    private final LnAuthService lnAuthService;

    public LnurlAuthExampleApplicationConfig(LnAuthService lnAuthService) {
        this.lnAuthService = requireNonNull(lnAuthService);
    }

    @Bean
    @Profile("!test")
    public ApplicationRunner applicationHiddenServiceInfoRunner(HiddenServiceDefinition applicationHiddenServiceDefinition) {
        return args -> {
            /*String onionUrl = applicationHiddenServiceDefinition.getVirtualHost()
                    .map(val -> {
                        int port = applicationHiddenServiceDefinition.getVirtualPort();
                        if (port == 80) {
                            return "http://" + val;
                        } else if (port == 443) {
                            return "https://" + val;
                        }
                        return "http://" + val + ":" + applicationHiddenServiceDefinition.getVirtualPort();
                    }).orElseThrow();*/

            LnUrlAuth lnUrlAuth = lnAuthService.createLnUrlAuth();

            log.info("=================================================");
            log.info("===== LNURL_AUTH ================================");
            log.info("=================================================");
            log.info("login page: {}", new URIBuilder(lnUrlAuth.toUri()).setPath("/login").removeQuery().build());
            log.info("=================================================");
            log.info("example auth url: {}", lnUrlAuth.toUri().toString());
            log.info("=================================================");
        };
    }
}

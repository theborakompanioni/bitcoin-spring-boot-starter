package org.tbk.lightning.lnurl.example.lnurl;

import lombok.SneakyThrows;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.lightning.lnurl.example.LnurlAuthExampleApplicationProperties;
import org.tbk.lightning.lnurl.example.api.LnUrlAuthLoginApi;
import org.tbk.tor.hs.HiddenServiceDefinition;

import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class LnUrlAuthServiceConfig {

    @Autowired
    private LnurlAuthExampleApplicationProperties properties;

    @Bean
    @SneakyThrows(URISyntaxException.class)
    public LnAuthService simpleLnAuthService(K1Manager k1Manager) {
        URI callbackUrl = new URIBuilder(properties.getLnurlAuthBaseUrl())
                .setPath(LnUrlAuthLoginApi.lnurlAuthPath())
                .build();

        return new SimpleLnAuthService(callbackUrl, k1Manager);
    }
}

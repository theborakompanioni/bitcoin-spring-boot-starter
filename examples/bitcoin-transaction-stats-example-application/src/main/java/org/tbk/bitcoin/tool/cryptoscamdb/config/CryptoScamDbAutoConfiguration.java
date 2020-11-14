package org.tbk.bitcoin.tool.cryptoscamdb.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.tbk.bitcoin.tool.cryptoscamdb.client.CryptoScamDbClient;
import org.tbk.bitcoin.tool.cryptoscamdb.client.CryptoScamDbClientImpl;

import static java.util.Objects.requireNonNull;

@Configuration
@EnableConfigurationProperties(CryptoScamDbAutoConfigProperties.class)
@ConditionalOnProperty(value = "org.tbk.bitcoin.tool.cryptoscamdb.enabled", havingValue = "true", matchIfMissing = true)
public class CryptoScamDbAutoConfiguration {

    private CryptoScamDbAutoConfigProperties properties;

    public CryptoScamDbAutoConfiguration(CryptoScamDbAutoConfigProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Bean
    @ConditionalOnMissingBean(CryptoScamDbClient.class)
    public CryptoScamDbClientImpl cryptoScamDbClient() {
        RestTemplate restTemplate = new RestTemplateBuilder()
                .defaultHeader("User-Agent", properties.getUserAgent())
                .build();

        return new CryptoScamDbClientImpl(properties.getBaseUrl(), restTemplate);
    }
}

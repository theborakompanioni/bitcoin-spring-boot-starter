package org.tbk.bitcoin.tool.btcabuse.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.bitcoin.tool.btcabuse.client.BtcAbuseApiClient;
import org.tbk.bitcoin.tool.btcabuse.client.BtcAbuseApiClientImpl;

import static java.util.Objects.requireNonNull;

@Configuration
@EnableConfigurationProperties(BtcAbuseAutoConfigProperties.class)
@ConditionalOnProperty(value = "org.tbk.bitcoin.tool.btcabuse.enabled", havingValue = "true")
public class BtcAbuseAutoConfiguration {

    private BtcAbuseAutoConfigProperties properties;

    public BtcAbuseAutoConfiguration(BtcAbuseAutoConfigProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Bean
    @ConditionalOnMissingBean(BtcAbuseApiClient.class)
    public BtcAbuseApiClient btcAbuseApiClient() {
        return new BtcAbuseApiClientImpl(properties.getBaseUrl(), properties.getApiToken());
    }
}

package org.tbk.bitcoin.btcabuse.config;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.bitcoin.btcabuse.client.BtcAbuseApiClient;
import org.tbk.bitcoin.btcabuse.client.BtcAbuseApiClientImpl;

import static java.util.Objects.requireNonNull;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(BtcAbuseClientAutoConfigProperties.class)
@ConditionalOnProperty(value = "org.tbk.bitcoin.tool.btcabuse.client.enabled", havingValue = "true")
public class BtcAbuseClientAutoConfiguration {

    private final BtcAbuseClientAutoConfigProperties properties;

    public BtcAbuseClientAutoConfiguration(BtcAbuseClientAutoConfigProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Bean(name = "btcAbuseHttpClient", destroyMethod = "close")
    @ConditionalOnMissingBean(name = "btcAbuseHttpClient")
    public CloseableHttpClient btcAbuseHttpClient() {
        return HttpClients.createDefault();
    }

    @Bean
    @ConditionalOnMissingBean(BtcAbuseApiClient.class)
    public BtcAbuseApiClient btcAbuseApiClient(@Qualifier("btcAbuseHttpClient") CloseableHttpClient btcAbuseHttpClient) {
        return new BtcAbuseApiClientImpl(btcAbuseHttpClient, properties.getBaseUrl(), properties.getApiToken());
    }
}

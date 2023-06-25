package org.tbk.bitcoin.tool.fee.btcdotcom.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.bitcoin.tool.fee.btcdotcom.BtcdotcomFeeApiClient;
import org.tbk.bitcoin.tool.fee.btcdotcom.BtcdotcomFeeApiClientImpl;
import org.tbk.bitcoin.tool.fee.btcdotcom.BtcdotcomFeeProvider;

import static java.util.Objects.requireNonNull;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(BtcdotcomFeeClientAutoConfigProperties.class)
@ConditionalOnClass({
        BtcdotcomFeeApiClient.class,
        BtcdotcomFeeProvider.class
})
@ConditionalOnProperty(name = {
        "org.tbk.bitcoin.tool.fee.enabled",
        "org.tbk.bitcoin.tool.fee.btcdotcom.enabled"
}, havingValue = "true", matchIfMissing = true)
public class BtcdotcomFeeClientAutoConfiguration {

    private final BtcdotcomFeeClientAutoConfigProperties properties;

    public BtcdotcomFeeClientAutoConfiguration(BtcdotcomFeeClientAutoConfigProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Bean
    @ConditionalOnMissingBean(BtcdotcomFeeApiClient.class)
    BtcdotcomFeeApiClient btcdotcomFeeApiClient() {
        return new BtcdotcomFeeApiClientImpl(properties.getBaseUrl(), properties.getToken().orElse(null));
    }

    @Bean
    @ConditionalOnMissingBean(BtcdotcomFeeProvider.class)
    BtcdotcomFeeProvider btcdotcomFeeProvider(BtcdotcomFeeApiClient btcdotcomFeeApiClient) {
        return new BtcdotcomFeeProvider(btcdotcomFeeApiClient);
    }
}

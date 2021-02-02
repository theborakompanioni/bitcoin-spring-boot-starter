package org.tbk.bitcoin.tool.fee.mempoolspace.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.bitcoin.tool.fee.btcdotcom.BtcdotcomFeeApiClient;
import org.tbk.bitcoin.tool.fee.btcdotcom.BtcdotcomFeeProvider;
import org.tbk.bitcoin.tool.fee.btcdotcom.config.BtcdotcomFeeClientAutoConfigProperties;
import org.tbk.bitcoin.tool.fee.mempoolspace.MempoolspaceFeeApiClient;
import org.tbk.bitcoin.tool.fee.mempoolspace.MempoolspaceFeeApiClientImpl;
import org.tbk.bitcoin.tool.fee.mempoolspace.MempoolspaceFeeProvider;

import static java.util.Objects.requireNonNull;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(MempoolspaceFeeClientAutoConfigProperties.class)
@ConditionalOnClass({
        BtcdotcomFeeApiClient.class,
        BtcdotcomFeeProvider.class
})
@ConditionalOnProperty(name = {
        "org.tbk.bitcoin.tool.fee.enabled",
        "org.tbk.bitcoin.tool.fee.mempoolspace.enabled"
}, havingValue = "true", matchIfMissing = true)
public class MempoolspaceFeeClientAutoConfiguration {

    private final MempoolspaceFeeClientAutoConfigProperties properties;

    public MempoolspaceFeeClientAutoConfiguration(MempoolspaceFeeClientAutoConfigProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Bean
    @ConditionalOnMissingBean(MempoolspaceFeeApiClient.class)
    public MempoolspaceFeeApiClient mempoolspaceFeeApiClient() {
        return new MempoolspaceFeeApiClientImpl(properties.getBaseUrl(), properties.getToken().orElse(null));
    }

    @Bean
    @ConditionalOnMissingBean(MempoolspaceFeeProvider.class)
    public MempoolspaceFeeProvider mempoolspaceFeeProvider(MempoolspaceFeeApiClient mempoolspaceFeeApiClient) {
        return new MempoolspaceFeeProvider(mempoolspaceFeeApiClient);
    }
}

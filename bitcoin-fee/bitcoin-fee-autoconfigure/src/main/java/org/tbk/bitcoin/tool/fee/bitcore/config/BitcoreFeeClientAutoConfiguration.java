package org.tbk.bitcoin.tool.fee.bitcore.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.tbk.bitcoin.tool.fee.bitcore.BitcoreFeeApiClient;
import org.tbk.bitcoin.tool.fee.bitcore.BitcoreFeeApiClientImpl;
import org.tbk.bitcoin.tool.fee.bitcore.BitcoreFeeProvider;

import static java.util.Objects.requireNonNull;

@AutoConfiguration
@EnableConfigurationProperties(BitcoreFeeClientAutoConfigProperties.class)
@ConditionalOnClass({
        BitcoreFeeApiClient.class,
        BitcoreFeeProvider.class
})
@ConditionalOnProperty(name = {
        "org.tbk.bitcoin.tool.fee.enabled",
        "org.tbk.bitcoin.tool.fee.bitcore.enabled"
}, havingValue = "true", matchIfMissing = true)
public class BitcoreFeeClientAutoConfiguration {

    private final BitcoreFeeClientAutoConfigProperties properties;

    public BitcoreFeeClientAutoConfiguration(BitcoreFeeClientAutoConfigProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Bean
    @ConditionalOnMissingBean(BitcoreFeeApiClient.class)
    BitcoreFeeApiClient bitcoreFeeApiClient() {
        return new BitcoreFeeApiClientImpl(properties.getBaseUrl(), properties.getToken().orElse(null));
    }

    @Bean
    @ConditionalOnMissingBean(BitcoreFeeProvider.class)
    BitcoreFeeProvider bitcoreFeeProvider(BitcoreFeeApiClient bitcoreFeeApiClient) {
        return new BitcoreFeeProvider(bitcoreFeeApiClient);
    }

}

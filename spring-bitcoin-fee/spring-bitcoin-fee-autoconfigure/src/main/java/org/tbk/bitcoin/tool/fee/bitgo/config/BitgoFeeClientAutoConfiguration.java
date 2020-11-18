package org.tbk.bitcoin.tool.fee.bitgo.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.bitcoin.tool.fee.bitcoinerlive.BitcoinerliveFeeProvider;
import org.tbk.bitcoin.tool.fee.bitgo.BitgoFeeApiClient;
import org.tbk.bitcoin.tool.fee.bitgo.BitgoFeeApiClientImpl;
import org.tbk.bitcoin.tool.fee.bitgo.BitgoFeeProvider;

import static java.util.Objects.requireNonNull;

@Configuration
@EnableConfigurationProperties(BitgoFeeClientAutoConfigProperties.class)
@ConditionalOnProperty(name = {
        "org.tbk.bitcoin.tool.fee.enabled",
        "org.tbk.bitcoin.tool.fee.bitgo.enabled"
}, havingValue = "true", matchIfMissing = true)
public class BitgoFeeClientAutoConfiguration {

    private final BitgoFeeClientAutoConfigProperties properties;

    public BitgoFeeClientAutoConfiguration(BitgoFeeClientAutoConfigProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Bean
    @ConditionalOnClass(BitgoFeeApiClient.class)
    @ConditionalOnMissingBean(BitgoFeeApiClient.class)
    public BitgoFeeApiClient bitgoFeeApiClient() {
        return new BitgoFeeApiClientImpl(properties.getBaseUrl(), properties.getToken().orElse(null));
    }

    @Bean
    @ConditionalOnClass(BitgoFeeProvider.class)
    @ConditionalOnMissingBean(BitgoFeeProvider.class)
    public BitgoFeeProvider bitgoFeeProvider(BitgoFeeApiClient bitgoFeeApiClient) {
        return new BitgoFeeProvider(bitgoFeeApiClient);
    }

}

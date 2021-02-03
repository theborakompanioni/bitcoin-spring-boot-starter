package org.tbk.bitcoin.tool.fee.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.tbk.bitcoin.tool.fee.CompositeFeeProvider;
import org.tbk.bitcoin.tool.fee.FeeProvider;

import java.util.List;

import static java.util.Objects.requireNonNull;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(BitcoinFeeClientAutoConfigProperties.class)
@ConditionalOnProperty(value = "org.tbk.bitcoin.tool.fee.enabled", havingValue = "true", matchIfMissing = true)
public class BitcoinFeeClientAutoConfiguration {

    private final BitcoinFeeClientAutoConfigProperties properties;

    public BitcoinFeeClientAutoConfiguration(BitcoinFeeClientAutoConfigProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Primary
    @Bean
    @ConditionalOnMissingBean(CompositeFeeProvider.class)
    public FeeProvider compositeFeeProvider(List<FeeProvider> feeProviders) {
        return new CompositeFeeProvider(feeProviders);
    }

}

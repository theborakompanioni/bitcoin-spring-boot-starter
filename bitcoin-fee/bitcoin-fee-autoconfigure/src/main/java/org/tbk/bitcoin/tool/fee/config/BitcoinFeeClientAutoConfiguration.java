package org.tbk.bitcoin.tool.fee.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.tbk.bitcoin.tool.fee.CompositeFeeProvider;
import org.tbk.bitcoin.tool.fee.FeeProvider;

import java.util.List;

import static java.util.Objects.requireNonNull;

@AutoConfiguration
@EnableConfigurationProperties(BitcoinFeeClientAutoConfigProperties.class)
@ConditionalOnProperty(value = "org.tbk.bitcoin.tool.fee.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass(CompositeFeeProvider.class)
public class BitcoinFeeClientAutoConfiguration {

    public BitcoinFeeClientAutoConfiguration(BitcoinFeeClientAutoConfigProperties properties) {
        requireNonNull(properties);
    }

    @Primary
    @Bean
    @ConditionalOnMissingBean(CompositeFeeProvider.class)
    FeeProvider compositeFeeProvider(List<FeeProvider> feeProviders) {
        return new CompositeFeeProvider(feeProviders);
    }

}

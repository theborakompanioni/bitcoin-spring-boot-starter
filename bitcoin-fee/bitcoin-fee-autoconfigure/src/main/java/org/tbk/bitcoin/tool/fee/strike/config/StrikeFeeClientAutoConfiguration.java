package org.tbk.bitcoin.tool.fee.strike.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.tbk.bitcoin.tool.fee.strike.StrikeFeeApiClient;
import org.tbk.bitcoin.tool.fee.strike.StrikeFeeApiClientImpl;
import org.tbk.bitcoin.tool.fee.strike.StrikeFeeProvider;

import static java.util.Objects.requireNonNull;

@AutoConfiguration
@EnableConfigurationProperties(StrikeFeeClientAutoConfigProperties.class)
@ConditionalOnClass({
        StrikeFeeApiClient.class
})
@ConditionalOnProperty(name = {
        "org.tbk.bitcoin.tool.fee.enabled",
        "org.tbk.bitcoin.tool.fee.strike.enabled"
}, havingValue = "true", matchIfMissing = true)
public class StrikeFeeClientAutoConfiguration {

    private final StrikeFeeClientAutoConfigProperties properties;

    public StrikeFeeClientAutoConfiguration(StrikeFeeClientAutoConfigProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Bean
    @ConditionalOnMissingBean(StrikeFeeApiClient.class)
    StrikeFeeApiClient strikeFeeApiClient() {
        return new StrikeFeeApiClientImpl(properties.getBaseUrl());
    }

    @Bean
    @ConditionalOnMissingBean(StrikeFeeProvider.class)
    StrikeFeeProvider strikeFeeProvider(StrikeFeeApiClient strikeFeeApiClient) {
        return new StrikeFeeProvider(strikeFeeApiClient);
    }
}

package org.tbk.bitcoin.tool.fee.blockchair.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.bitcoin.tool.fee.blockchair.BlockchairFeeApiClient;
import org.tbk.bitcoin.tool.fee.blockchair.BlockchairFeeApiClientImpl;
import org.tbk.bitcoin.tool.fee.blockchair.BlockchairFeeProvider;

import static java.util.Objects.requireNonNull;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(BlockchairFeeClientAutoConfigProperties.class)
@ConditionalOnClass({
        BlockchairFeeApiClient.class,
        BlockchairFeeProvider.class
})
@ConditionalOnProperty(name = {
        "org.tbk.bitcoin.tool.fee.enabled",
        "org.tbk.bitcoin.tool.fee.blockchair.enabled"
}, havingValue = "true", matchIfMissing = true)
public class BlockchairFeeClientAutoConfiguration {

    private final BlockchairFeeClientAutoConfigProperties properties;

    public BlockchairFeeClientAutoConfiguration(BlockchairFeeClientAutoConfigProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Bean
    @ConditionalOnMissingBean(BlockchairFeeApiClient.class)
    BlockchairFeeApiClient blockchairFeeApiClient() {
        return new BlockchairFeeApiClientImpl(properties.getBaseUrl(), properties.getToken().orElse(null));
    }

    @Bean
    @ConditionalOnMissingBean(BlockchairFeeProvider.class)
    BlockchairFeeProvider blockchairFeeProvider(BlockchairFeeApiClient blockchairFeeApiClientb) {
        return new BlockchairFeeProvider(blockchairFeeApiClientb);
    }
}

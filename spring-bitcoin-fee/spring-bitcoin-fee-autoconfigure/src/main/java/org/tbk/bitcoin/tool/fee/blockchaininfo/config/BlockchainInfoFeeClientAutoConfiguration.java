package org.tbk.bitcoin.tool.fee.blockchaininfo.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.bitcoin.tool.fee.blockchaininfo.BlockchainInfoFeeApiClient;
import org.tbk.bitcoin.tool.fee.blockchaininfo.BlockchainInfoFeeApiClientImpl;
import org.tbk.bitcoin.tool.fee.blockchaininfo.BlockchainInfoFeeProvider;

import static java.util.Objects.requireNonNull;

@Configuration
@EnableConfigurationProperties(BlockchainInfoFeeClientAutoConfigProperties.class)
@ConditionalOnClass({
        BlockchainInfoFeeApiClient.class,
        BlockchainInfoFeeProvider.class
})
@ConditionalOnProperty(name = {
        "org.tbk.bitcoin.tool.fee.enabled",
        "org.tbk.bitcoin.tool.fee.blockchaininfo.enabled"
}, havingValue = "true", matchIfMissing = true)
public class BlockchainInfoFeeClientAutoConfiguration {

    private final BlockchainInfoFeeClientAutoConfigProperties properties;

    public BlockchainInfoFeeClientAutoConfiguration(BlockchainInfoFeeClientAutoConfigProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Bean
    @ConditionalOnMissingBean(BlockchainInfoFeeApiClient.class)
    public BlockchainInfoFeeApiClient blockchainInfoFeeApiClient() {
        return new BlockchainInfoFeeApiClientImpl(properties.getBaseUrl(), properties.getToken().orElse(null));
    }

    @Bean
    @ConditionalOnMissingBean(BlockchainInfoFeeProvider.class)
    public BlockchainInfoFeeProvider blockchainInfoFeeProvider(BlockchainInfoFeeApiClient blockchainInfoFeeApiClient) {
        return new BlockchainInfoFeeProvider(blockchainInfoFeeApiClient);
    }
}

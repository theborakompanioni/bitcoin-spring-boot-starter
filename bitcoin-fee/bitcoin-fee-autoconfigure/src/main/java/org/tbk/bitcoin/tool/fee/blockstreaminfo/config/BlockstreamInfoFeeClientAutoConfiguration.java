package org.tbk.bitcoin.tool.fee.blockstreaminfo.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.tbk.bitcoin.tool.fee.blockstreaminfo.BlockstreamInfoFeeApiClient;
import org.tbk.bitcoin.tool.fee.blockstreaminfo.BlockstreamInfoFeeApiClientImpl;
import org.tbk.bitcoin.tool.fee.blockstreaminfo.BlockstreamInfoFeeProvider;

import static java.util.Objects.requireNonNull;

@AutoConfiguration
@EnableConfigurationProperties(BlockstreamInfoFeeClientAutoConfigProperties.class)
@ConditionalOnClass({
        BlockstreamInfoFeeApiClient.class,
        BlockstreamInfoFeeProvider.class
})
@ConditionalOnProperty(name = {
        "org.tbk.bitcoin.tool.fee.enabled",
        "org.tbk.bitcoin.tool.fee.blockstreaminfo.enabled"
}, havingValue = "true", matchIfMissing = true)
public class BlockstreamInfoFeeClientAutoConfiguration {

    private final BlockstreamInfoFeeClientAutoConfigProperties properties;

    public BlockstreamInfoFeeClientAutoConfiguration(BlockstreamInfoFeeClientAutoConfigProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Bean
    @ConditionalOnMissingBean(BlockstreamInfoFeeApiClient.class)
    BlockstreamInfoFeeApiClient blockstreamInfoFeeApiClient() {
        return new BlockstreamInfoFeeApiClientImpl(properties.getBaseUrl(), properties.getToken().orElse(null));
    }

    @Bean
    @ConditionalOnMissingBean(BlockstreamInfoFeeProvider.class)
    BlockstreamInfoFeeProvider blockstreamInfoFeeProvider(BlockstreamInfoFeeApiClient blockstreamInfoFeeApiClient) {
        return new BlockstreamInfoFeeProvider(blockstreamInfoFeeApiClient);
    }
}

package org.tbk.bitcoin.tool.fee.blockcypher.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.bitcoin.tool.fee.bitcoinerlive.BitcoinerliveFeeApiClient;
import org.tbk.bitcoin.tool.fee.bitcoinerlive.BitcoinerliveFeeApiClientImpl;
import org.tbk.bitcoin.tool.fee.bitcoinerlive.BitcoinerliveFeeProvider;
import org.tbk.bitcoin.tool.fee.bitcoinerlive.config.BitcoinerliveFeeClientAutoConfigProperties;
import org.tbk.bitcoin.tool.fee.blockcypher.BlockcypherFeeApiClient;
import org.tbk.bitcoin.tool.fee.blockcypher.BlockcypherFeeApiClientImpl;
import org.tbk.bitcoin.tool.fee.blockcypher.BlockcypherFeeProvider;

import static java.util.Objects.requireNonNull;

@Configuration
@EnableConfigurationProperties(BlockcypherFeeClientAutoConfigProperties.class)
@ConditionalOnProperty(name = {
        "org.tbk.bitcoin.tool.fee.enabled",
        "org.tbk.bitcoin.tool.fee.blockcypher.enabled"
}, havingValue = "true", matchIfMissing = true)
public class BlockcypherFeeClientAutoConfiguration {

    private final BlockcypherFeeClientAutoConfigProperties properties;

    public BlockcypherFeeClientAutoConfiguration(BlockcypherFeeClientAutoConfigProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Bean
    @ConditionalOnClass(BlockcypherFeeApiClient.class)
    @ConditionalOnMissingBean(BlockcypherFeeApiClient.class)
    public BlockcypherFeeApiClient blockcypherFeeApiClient() {
        return new BlockcypherFeeApiClientImpl(properties.getBaseUrl(), properties.getToken().orElse(null));
    }

    @Bean
    @ConditionalOnClass(BlockcypherFeeProvider.class)
    @ConditionalOnMissingBean(BlockcypherFeeProvider.class)
    public BlockcypherFeeProvider blockcypherFeeProvider(BlockcypherFeeApiClient blockcypherFeeApiClient) {
        return new BlockcypherFeeProvider(blockcypherFeeApiClient);
    }
}

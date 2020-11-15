package org.tbk.bitcoin.tool.fee.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.bitcoin.tool.fee.blockchaininfo.BlockchainInfoFeeProvider;
import org.tbk.bitcoin.tool.fee.earndotcom.EarndotcomFeeProvider;

import static java.util.Objects.requireNonNull;

@Configuration
@EnableConfigurationProperties(BitcoinFeeClientAutoConfigProperties.class)
@ConditionalOnProperty(value = "org.tbk.bitcoin.tool.fee.enabled", havingValue = "true", matchIfMissing = true)
public class BitcoinFeeClientAutoConfiguration {

    private BitcoinFeeClientAutoConfigProperties properties;

    public BitcoinFeeClientAutoConfiguration(BitcoinFeeClientAutoConfigProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Bean
    @ConditionalOnClass(EarndotcomFeeProvider.class)
    @ConditionalOnMissingBean(EarndotcomFeeProvider.class)
    public EarndotcomFeeProvider earndotcomFeeProvider() {
        return new EarndotcomFeeProvider();
    }

    @Bean
    @ConditionalOnClass(BlockchainInfoFeeProvider.class)
    @ConditionalOnMissingBean(BlockchainInfoFeeProvider.class)
    public BlockchainInfoFeeProvider blockchainInfoFeeProvider() {
        return new BlockchainInfoFeeProvider();
    }


}

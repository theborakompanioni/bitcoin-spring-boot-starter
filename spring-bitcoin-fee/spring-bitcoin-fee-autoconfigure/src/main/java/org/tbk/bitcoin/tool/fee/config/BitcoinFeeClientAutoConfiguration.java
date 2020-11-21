package org.tbk.bitcoin.tool.fee.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.tbk.bitcoin.tool.fee.CompositeFeeProvider;
import org.tbk.bitcoin.tool.fee.FeeProvider;
import org.tbk.bitcoin.tool.fee.blockchair.BlockchairFeeApiClient;
import org.tbk.bitcoin.tool.fee.blockchair.BlockchairFeeApiClientImpl;
import org.tbk.bitcoin.tool.fee.blockchair.BlockchairFeeProvider;
import org.tbk.bitcoin.tool.fee.blockcypher.BlockcypherFeeApiClient;
import org.tbk.bitcoin.tool.fee.blockcypher.BlockcypherFeeApiClientImpl;
import org.tbk.bitcoin.tool.fee.blockcypher.BlockcypherFeeProvider;
import org.tbk.bitcoin.tool.fee.btcdotcom.BtcdotcomFeeApiClient;
import org.tbk.bitcoin.tool.fee.btcdotcom.BtcdotcomFeeApiClientImpl;
import org.tbk.bitcoin.tool.fee.btcdotcom.BtcdotcomFeeProvider;

import java.util.List;

import static java.util.Objects.requireNonNull;

@Configuration
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
    public CompositeFeeProvider compositeFeeProvider(List<FeeProvider> feeProviders) {
        return new CompositeFeeProvider(feeProviders);
    }

    // TODO: move to own package
    @Bean
    @ConditionalOnClass(BlockchairFeeApiClient.class)
    @ConditionalOnMissingBean(BlockchairFeeApiClient.class)
    public BlockchairFeeApiClient blockchairFeeApiClient() {
        return new BlockchairFeeApiClientImpl("https://api.blockchair.com", null);
    }

    // TODO: move to own package
    @Bean
    @ConditionalOnClass(BlockchairFeeProvider.class)
    @ConditionalOnMissingBean(BlockchairFeeProvider.class)
    public BlockchairFeeProvider blockchairFeeProvider(BlockchairFeeApiClient blockchairFeeApiClientb) {
        return new BlockchairFeeProvider(blockchairFeeApiClientb);
    }

    // TODO: move to own package
    @Bean
    @ConditionalOnClass(BlockcypherFeeApiClient.class)
    @ConditionalOnMissingBean(BlockcypherFeeApiClient.class)
    public BlockcypherFeeApiClient blockcypherFeeApiClient() {
        return new BlockcypherFeeApiClientImpl("https://api.blockcypher.com", null);
    }

    // TODO: move to own package
    @Bean
    @ConditionalOnClass(BlockcypherFeeProvider.class)
    @ConditionalOnMissingBean(BlockcypherFeeProvider.class)
    public BlockcypherFeeProvider blockcypherFeeProvider(BlockcypherFeeApiClient blockcypherFeeApiClient) {
        return new BlockcypherFeeProvider(blockcypherFeeApiClient);
    }

    // TODO: move to own package
    @Bean
    @ConditionalOnClass(BtcdotcomFeeApiClient.class)
    @ConditionalOnMissingBean(BtcdotcomFeeApiClient.class)
    public BtcdotcomFeeApiClient btcdotcomFeeApiClient() {
        return new BtcdotcomFeeApiClientImpl("https://btc.com", null);
    }

    // TODO: move to own package
    @Bean
    @ConditionalOnClass(BtcdotcomFeeProvider.class)
    @ConditionalOnMissingBean(BtcdotcomFeeProvider.class)
    public BtcdotcomFeeProvider btcdotcomFeeProvider(BtcdotcomFeeApiClient btcdotcomFeeApiClient) {
        return new BtcdotcomFeeProvider(btcdotcomFeeApiClient);
    }

}

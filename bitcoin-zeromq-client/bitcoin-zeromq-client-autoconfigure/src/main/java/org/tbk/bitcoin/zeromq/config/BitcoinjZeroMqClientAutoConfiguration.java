package org.tbk.bitcoin.zeromq.config;

import org.bitcoinj.core.BitcoinSerializer;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.bitcoin.zeromq.bitcoinj.BitcoinjBlockPublisherFactory;
import org.tbk.bitcoin.zeromq.bitcoinj.BitcoinjTransactionPublisherFactory;
import org.tbk.bitcoin.zeromq.client.MessagePublishService;
import org.tbk.bitcoin.zeromq.client.ZeroMqMessagePublisherFactory;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(BitcoinZeroMqClientAutoConfigurationProperties.class)
@AutoConfigureAfter(BitcoinZeroMqClientAutoConfiguration.class)
@ConditionalOnProperty(value = "org.tbk.bitcoin.zeromq.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass(BitcoinjBlockPublisherFactory.class)
public class BitcoinjZeroMqClientAutoConfiguration {

    private static NetworkParameters networkFromProperties(BitcoinZmqClientConfig bitcoinZmqClientConfig) {
        return switch (bitcoinZmqClientConfig.getNetwork()) {
            case mainnet -> MainNetParams.get();
            case testnet -> TestNet3Params.get();
            case regtest -> RegTestParams.get();
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public BitcoinSerializer bitcoinjZeroMqClientBitcoinSerializer(BitcoinZmqClientConfig bitcoinZmqClientConfig) {
        NetworkParameters networkParameters = networkFromProperties(bitcoinZmqClientConfig);
        return new BitcoinSerializer(networkParameters, false);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(name = "bitcoinRawTxZeroMqMessagePublisherFactory")
    public BitcoinjTransactionPublisherFactory bitcoinjTransactionPublisherFactory(
            BitcoinSerializer bitcoinSerializer,
            @Qualifier("bitcoinRawTxZeroMqMessagePublisherFactory") ZeroMqMessagePublisherFactory bitcoinRawTxZeroMqMessagePublisherFactory
    ) {
        return new BitcoinjTransactionPublisherFactory(bitcoinSerializer, bitcoinRawTxZeroMqMessagePublisherFactory);
    }

    @Bean(initMethod = "startAsync", destroyMethod = "stopAsync")
    @ConditionalOnMissingBean(value = Transaction.class, parameterizedContainer = MessagePublishService.class)
    @ConditionalOnBean(BitcoinjTransactionPublisherFactory.class)
    public MessagePublishService<Transaction> bitcoinjTransactionPublishService(
            BitcoinjTransactionPublisherFactory bitcoinjTransactionPublisherFactory
    ) {
        return new MessagePublishService<>(bitcoinjTransactionPublisherFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(name = "bitcoinRawBlockZeroMqMessagePublisherFactory")
    public BitcoinjBlockPublisherFactory bitcoinjBlockPublisherFactory(
            BitcoinSerializer bitcoinSerializer,
            @Qualifier("bitcoinRawBlockZeroMqMessagePublisherFactory") ZeroMqMessagePublisherFactory bitcoinRawBlockZeroMqMessagePublisherFactory
    ) {
        return new BitcoinjBlockPublisherFactory(bitcoinSerializer, bitcoinRawBlockZeroMqMessagePublisherFactory);
    }

    @Bean(initMethod = "startAsync", destroyMethod = "stopAsync")
    @ConditionalOnMissingBean(value = Block.class, parameterizedContainer = MessagePublishService.class)
    @ConditionalOnBean(BitcoinjBlockPublisherFactory.class)
    public MessagePublishService<Block> bitcoinjBlockPublishService(
            BitcoinjBlockPublisherFactory bitcoinjBlockPublisherFactory
    ) {
        return new MessagePublishService<>(bitcoinjBlockPublisherFactory);
    }
}

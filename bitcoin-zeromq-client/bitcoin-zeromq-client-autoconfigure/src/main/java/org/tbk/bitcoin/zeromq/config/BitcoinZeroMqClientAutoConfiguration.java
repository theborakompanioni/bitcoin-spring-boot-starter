package org.tbk.bitcoin.zeromq.config;

import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.bitcoin.zeromq.bitcoinj.BitcoinjTransactionPublisherFactory;
import org.tbk.bitcoin.zeromq.client.BitcoinZeroMqTopics;
import org.tbk.bitcoin.zeromq.client.MessagePublishService;
import org.tbk.bitcoin.zeromq.client.ZeroMqMessagePublisherFactory;

import static java.util.Objects.requireNonNull;

@Slf4j
@Configuration
@EnableConfigurationProperties(BitcoinZeroMqClientProperties.class)
@ConditionalOnProperty(value = "org.tbk.bitcoin.zeromq.enabled", havingValue = "true", matchIfMissing = true)
public class BitcoinZeroMqClientAutoConfiguration {

    private final BitcoinZeroMqClientProperties properties;

    public BitcoinZeroMqClientAutoConfiguration(BitcoinZeroMqClientProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public NetworkParameters bitcoinZeroMqNetworkParameters() {
        switch (properties.getNetwork()) {
            case mainnet:
                return MainNetParams.get();
            case testnet:
                return TestNet3Params.get();
            case regtest:
                return RegTestParams.get();
        }
        throw new IllegalArgumentException();
    }

    @Bean("bitcoinRawTxZeroMqMessagePublisherFactory")
    @ConditionalOnProperty(name = "org.tbk.bitcoin.zeromq.zmqpubrawtx")
    public ZeroMqMessagePublisherFactory bitcoinRawTxZeroMqMessagePublisherFactory() {
        return properties.getZmqpubrawtx()
                .map(val -> ZeroMqMessagePublisherFactory.builder()
                        .topic(BitcoinZeroMqTopics.rawtx())
                        .address(val)
                        .build())
                .orElseThrow(() -> new IllegalStateException("Could not create bean from 'zmqpubrawtx'"));
    }

    @Bean("bitcoinRawBlockZeroMqMessagePublisherFactory")
    @ConditionalOnProperty(name = "org.tbk.bitcoin.zeromq.zmqpubrawblock")
    public ZeroMqMessagePublisherFactory bitcoinRawBlockZeroMqMessagePublisherFactory() {
        return properties.getZmqpubrawblock()
                .map(val -> ZeroMqMessagePublisherFactory.builder()
                        .topic(BitcoinZeroMqTopics.rawblock())
                        .address(val)
                        .build())
                .orElseThrow(() -> new IllegalStateException("Could not create bean from 'zmqpubrawblock'"));
    }

    @Bean("bitcoinHashBlockZeroMqMessagePublisherFactory")
    @ConditionalOnProperty(name = "org.tbk.bitcoin.zeromq.zmqpubhashblock")
    public ZeroMqMessagePublisherFactory bitcoinHashBlockZeroMqMessagePublisherFactory() {
        return properties.getZmqpubhashblock()
                .map(val -> ZeroMqMessagePublisherFactory.builder()
                        .topic(BitcoinZeroMqTopics.hashblock())
                        .address(val)
                        .build())
                .orElseThrow(() -> new IllegalStateException("Could not create bean from 'zmqpubhashblock'"));
    }

    @Bean("bitcoinHashTxZeroMqMessagePublisherFactory")
    @ConditionalOnProperty(name = "org.tbk.bitcoin.zeromq.zmqpubhashtx")
    public ZeroMqMessagePublisherFactory bitcoinHashTxZeroMqMessagePublisherFactory() {
        return properties.getZmqpubhashtx()
                .map(val -> ZeroMqMessagePublisherFactory.builder()
                        .topic(BitcoinZeroMqTopics.hashtx())
                        .address(val)
                        .build())
                .orElseThrow(() -> new IllegalStateException("Could not create bean from 'zmqpubhashtx'"));
    }

    @Bean("bitcoinjTransactionPublisherFactory")
    @ConditionalOnMissingBean
    @ConditionalOnBean(name = "bitcoinRawTxZeroMqMessagePublisherFactory")
    public BitcoinjTransactionPublisherFactory bitcoinjTransactionPublisherFactory(
            NetworkParameters networkParameters,
            @Qualifier("bitcoinRawTxZeroMqMessagePublisherFactory") ZeroMqMessagePublisherFactory bitcoinRawTxZeroMqMessagePublisherFactory
    ) {
        return new BitcoinjTransactionPublisherFactory(networkParameters, bitcoinRawTxZeroMqMessagePublisherFactory);
    }

    @Bean(name = "bitcoinjTransactionPublishService", initMethod = "startAsync", destroyMethod = "stopAsync")
    @ConditionalOnBean(BitcoinjTransactionPublisherFactory.class)
    public MessagePublishService<Transaction> bitcoinjTransactionPublishService(
            BitcoinjTransactionPublisherFactory bitcoinjTransactionPublisherFactory
    ) {
        return new MessagePublishService<>(bitcoinjTransactionPublisherFactory);
    }
}

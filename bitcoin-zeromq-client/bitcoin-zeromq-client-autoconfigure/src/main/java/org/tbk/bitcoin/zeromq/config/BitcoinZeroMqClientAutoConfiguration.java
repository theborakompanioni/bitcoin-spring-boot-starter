package org.tbk.bitcoin.zeromq.config;

import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.BitcoinSerializer;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.bitcoin.zeromq.bitcoinj.BitcoinjBlockPublisherFactory;
import org.tbk.bitcoin.zeromq.bitcoinj.BitcoinjTransactionPublisherFactory;
import org.tbk.bitcoin.zeromq.client.BitcoinZeroMqTopics;
import org.tbk.bitcoin.zeromq.client.MessagePublishService;
import org.tbk.bitcoin.zeromq.client.ZeroMqMessagePublisherFactory;
import org.tbk.bitcoin.zeromq.config.BitcoinZmqClientConfig.BitcoinZmqClientConfigBuilder;

import static java.util.Objects.requireNonNull;

@Slf4j
@Configuration
@EnableConfigurationProperties(BitcoinZeroMqClientAutoConfigurationProperties.class)
@ConditionalOnProperty(value = "org.tbk.bitcoin.zeromq.enabled", havingValue = "true", matchIfMissing = true)
public class BitcoinZeroMqClientAutoConfiguration {

    private final BitcoinZeroMqClientAutoConfigurationProperties properties;

    public BitcoinZeroMqClientAutoConfiguration(BitcoinZeroMqClientAutoConfigurationProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public BitcoinZmqClientConfig bitcoinZmqClientConfig(
            ObjectProvider<BitcoinZmqClientConfigBuilderCustomizer> bitcoinZmqClientConfigBuilderCustomizer) {

        BitcoinZmqClientConfigBuilder configBuilder = BitcoinZmqClientConfig.builder()
                .network(networkFromProperties())
                .zmqpubhashblock(this.properties.getZmqpubhashblock().orElse(null))
                .zmqpubhashtx(this.properties.getZmqpubhashtx().orElse(null))
                .zmqpubrawblock(this.properties.getZmqpubrawblock().orElse(null))
                .zmqpubrawtx(this.properties.getZmqpubrawtx().orElse(null));

        bitcoinZmqClientConfigBuilderCustomizer.orderedStream().forEach(customizer -> customizer.customize(configBuilder));

        return configBuilder.build();
    }

    private NetworkParameters networkFromProperties() {
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

    @Bean
    @ConditionalOnMissingBean
    public NetworkParameters bitcoinZeroMqNetworkParameters(BitcoinZmqClientConfig bitcoinZmqClientConfig) {
        return bitcoinZmqClientConfig.getNetwork();
    }

    @Bean
    @ConditionalOnMissingBean
    public BitcoinSerializer bitcoinSerializer(NetworkParameters networkParameters) {
        return new BitcoinSerializer(networkParameters, false);
    }


    @Bean("bitcoinRawTxZeroMqMessagePublisherFactory")
    @ConditionalOnProperty(name = "org.tbk.bitcoin.zeromq.zmqpubrawtx")
    public ZeroMqMessagePublisherFactory bitcoinRawTxZeroMqMessagePublisherFactory(BitcoinZmqClientConfig bitcoinZmqClientConfig) {
        return bitcoinZmqClientConfig.getZmqpubrawtx()
                .map(val -> ZeroMqMessagePublisherFactory.builder()
                        .topic(BitcoinZeroMqTopics.rawtx())
                        .address(val)
                        .build())
                .orElseThrow(() -> new IllegalStateException("Could not create bean from 'zmqpubrawtx'"));
    }

    @Bean("bitcoinRawBlockZeroMqMessagePublisherFactory")
    @ConditionalOnProperty(name = "org.tbk.bitcoin.zeromq.zmqpubrawblock")
    public ZeroMqMessagePublisherFactory bitcoinRawBlockZeroMqMessagePublisherFactory(BitcoinZmqClientConfig bitcoinZmqClientConfig) {
        return bitcoinZmqClientConfig.getZmqpubrawblock()
                .map(val -> ZeroMqMessagePublisherFactory.builder()
                        .topic(BitcoinZeroMqTopics.rawblock())
                        .address(val)
                        .build())
                .orElseThrow(() -> new IllegalStateException("Could not create bean from 'zmqpubrawblock'"));
    }

    @Bean("bitcoinHashBlockZeroMqMessagePublisherFactory")
    @ConditionalOnProperty(name = "org.tbk.bitcoin.zeromq.zmqpubhashblock")
    public ZeroMqMessagePublisherFactory bitcoinHashBlockZeroMqMessagePublisherFactory(BitcoinZmqClientConfig bitcoinZmqClientConfig) {
        return bitcoinZmqClientConfig.getZmqpubhashblock()
                .map(val -> ZeroMqMessagePublisherFactory.builder()
                        .topic(BitcoinZeroMqTopics.hashblock())
                        .address(val)
                        .build())
                .orElseThrow(() -> new IllegalStateException("Could not create bean from 'zmqpubhashblock'"));
    }

    @Bean("bitcoinHashTxZeroMqMessagePublisherFactory")
    @ConditionalOnProperty(name = "org.tbk.bitcoin.zeromq.zmqpubhashtx")
    public ZeroMqMessagePublisherFactory bitcoinHashTxZeroMqMessagePublisherFactory(BitcoinZmqClientConfig bitcoinZmqClientConfig) {
        return bitcoinZmqClientConfig.getZmqpubhashtx()
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
            BitcoinSerializer bitcoinSerializer,
            @Qualifier("bitcoinRawTxZeroMqMessagePublisherFactory") ZeroMqMessagePublisherFactory bitcoinRawTxZeroMqMessagePublisherFactory
    ) {
        return new BitcoinjTransactionPublisherFactory(bitcoinSerializer, bitcoinRawTxZeroMqMessagePublisherFactory);
    }

    @Bean(name = "bitcoinjTransactionPublishService", initMethod = "startAsync", destroyMethod = "stopAsync")
    @ConditionalOnBean(BitcoinjTransactionPublisherFactory.class)
    public MessagePublishService<Transaction> bitcoinjTransactionPublishService(
            BitcoinjTransactionPublisherFactory bitcoinjTransactionPublisherFactory
    ) {
        return new MessagePublishService<>(bitcoinjTransactionPublisherFactory);
    }

    @Bean("bitcoinjBlockPublisherFactory")
    @ConditionalOnMissingBean
    @ConditionalOnBean(name = "bitcoinRawBlockZeroMqMessagePublisherFactory")
    public BitcoinjBlockPublisherFactory bitcoinjBlockPublisherFactory(
            BitcoinSerializer bitcoinSerializer,
            @Qualifier("bitcoinRawBlockZeroMqMessagePublisherFactory") ZeroMqMessagePublisherFactory bitcoinRawBlockZeroMqMessagePublisherFactory
    ) {
        return new BitcoinjBlockPublisherFactory(bitcoinSerializer, bitcoinRawBlockZeroMqMessagePublisherFactory);
    }

    @Bean(name = "bitcoinjBlockPublishService", initMethod = "startAsync", destroyMethod = "stopAsync")
    @ConditionalOnBean(BitcoinjBlockPublisherFactory.class)
    public MessagePublishService<Block> bitcoinjBlockPublishService(
            BitcoinjBlockPublisherFactory bitcoinjBlockPublisherFactory
    ) {
        return new MessagePublishService<>(bitcoinjBlockPublisherFactory);
    }
}

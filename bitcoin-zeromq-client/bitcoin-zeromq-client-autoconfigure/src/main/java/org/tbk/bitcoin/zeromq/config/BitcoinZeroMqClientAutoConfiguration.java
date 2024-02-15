package org.tbk.bitcoin.zeromq.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.tbk.bitcoin.zeromq.client.BitcoinZeroMqTopics;
import org.tbk.bitcoin.zeromq.client.ZeroMqMessagePublisherFactory;
import org.tbk.bitcoin.zeromq.config.BitcoinZmqClientConfig.BitcoinZmqClientConfigBuilder;

import static java.util.Objects.requireNonNull;

@AutoConfiguration
@EnableConfigurationProperties(BitcoinZeroMqClientAutoConfigurationProperties.class)
@ConditionalOnProperty(value = "org.tbk.bitcoin.zeromq.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass(ZeroMqMessagePublisherFactory.class)
public class BitcoinZeroMqClientAutoConfiguration {

    private final BitcoinZeroMqClientAutoConfigurationProperties properties;

    public BitcoinZeroMqClientAutoConfiguration(BitcoinZeroMqClientAutoConfigurationProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    BitcoinZmqClientConfig bitcoinZmqClientConfig(
            ObjectProvider<BitcoinZmqClientConfigBuilderCustomizer> bitcoinZmqClientConfigBuilderCustomizer) {

        BitcoinZmqClientConfigBuilder configBuilder = BitcoinZmqClientConfig.builder()
                .network(this.properties.getNetwork())
                .zmqpubhashblock(this.properties.getZmqpubhashblock().orElse(null))
                .zmqpubhashtx(this.properties.getZmqpubhashtx().orElse(null))
                .zmqpubrawblock(this.properties.getZmqpubrawblock().orElse(null))
                .zmqpubrawtx(this.properties.getZmqpubrawtx().orElse(null));

        bitcoinZmqClientConfigBuilderCustomizer.orderedStream().forEach(customizer -> customizer.customize(configBuilder));

        return configBuilder.build();
    }

    @Bean
    @ConditionalOnProperty(name = "org.tbk.bitcoin.zeromq.zmqpubrawblock")
    ZeroMqMessagePublisherFactory bitcoinRawBlockZeroMqMessagePublisherFactory(BitcoinZmqClientConfig bitcoinZmqClientConfig) {
        return bitcoinZmqClientConfig.getZmqpubrawblock()
                .map(val -> ZeroMqMessagePublisherFactory.builder()
                        .topic(BitcoinZeroMqTopics.rawblock())
                        .address(val)
                        .build())
                .orElseThrow(() -> new IllegalStateException("Could not create bean from 'zmqpubrawblock'"));
    }

    @Bean
    @ConditionalOnProperty(name = "org.tbk.bitcoin.zeromq.zmqpubrawtx")
    ZeroMqMessagePublisherFactory bitcoinRawTxZeroMqMessagePublisherFactory(BitcoinZmqClientConfig bitcoinZmqClientConfig) {
        return bitcoinZmqClientConfig.getZmqpubrawtx()
                .map(val -> ZeroMqMessagePublisherFactory.builder()
                        .topic(BitcoinZeroMqTopics.rawtx())
                        .address(val)
                        .build())
                .orElseThrow(() -> new IllegalStateException("Could not create bean from 'zmqpubrawtx'"));
    }

    @Bean
    @ConditionalOnProperty(name = "org.tbk.bitcoin.zeromq.zmqpubhashblock")
    ZeroMqMessagePublisherFactory bitcoinHashBlockZeroMqMessagePublisherFactory(BitcoinZmqClientConfig bitcoinZmqClientConfig) {
        return bitcoinZmqClientConfig.getZmqpubhashblock()
                .map(val -> ZeroMqMessagePublisherFactory.builder()
                        .topic(BitcoinZeroMqTopics.hashblock())
                        .address(val)
                        .build())
                .orElseThrow(() -> new IllegalStateException("Could not create bean from 'zmqpubhashblock'"));
    }

    @Bean
    @ConditionalOnProperty(name = "org.tbk.bitcoin.zeromq.zmqpubhashtx")
    ZeroMqMessagePublisherFactory bitcoinHashTxZeroMqMessagePublisherFactory(BitcoinZmqClientConfig bitcoinZmqClientConfig) {
        return bitcoinZmqClientConfig.getZmqpubhashtx()
                .map(val -> ZeroMqMessagePublisherFactory.builder()
                        .topic(BitcoinZeroMqTopics.hashtx())
                        .address(val)
                        .build())
                .orElseThrow(() -> new IllegalStateException("Could not create bean from 'zmqpubhashtx'"));
    }
}

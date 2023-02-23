package org.tbk.bitcoin.zeromq.config;

import fr.acinq.bitcoin.Block;
import fr.acinq.bitcoin.Transaction;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.bitcoin.zeromq.client.MessagePublishService;
import org.tbk.bitcoin.zeromq.client.ZeroMqMessagePublisherFactory;
import org.tbk.bitcoin.zeromq.kmp.KmpBlockPublisherFactory;
import org.tbk.bitcoin.zeromq.kmp.KmpTransactionPublisherFactory;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(BitcoinZeroMqClientAutoConfigurationProperties.class)
@AutoConfigureAfter(BitcoinZeroMqClientAutoConfiguration.class)
@ConditionalOnProperty(value = "org.tbk.bitcoin.zeromq.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass(KmpBlockPublisherFactory.class)
public class KmpZeroMqClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(name = "bitcoinRawBlockZeroMqMessagePublisherFactory")
    public KmpBlockPublisherFactory bitcoinKmpBlockPublisherFactory(
            @Qualifier("bitcoinRawBlockZeroMqMessagePublisherFactory") ZeroMqMessagePublisherFactory bitcoinRawBlockZeroMqMessagePublisherFactory
    ) {
        return new KmpBlockPublisherFactory(bitcoinRawBlockZeroMqMessagePublisherFactory);
    }

    @Bean(initMethod = "startAsync", destroyMethod = "stopAsync")
    @ConditionalOnMissingBean(value = Block.class, parameterizedContainer = MessagePublishService.class)
    @ConditionalOnBean(KmpBlockPublisherFactory.class)
    public MessagePublishService<Block> bitcoinKmpBlockPublishService(
            KmpBlockPublisherFactory bitcoinKmpBlockPublisherFactory
    ) {
        return new MessagePublishService<>(bitcoinKmpBlockPublisherFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(name = "bitcoinRawTxZeroMqMessagePublisherFactory")
    public KmpTransactionPublisherFactory bitcoinKmpTransactionPublisherFactory(
            @Qualifier("bitcoinRawTxZeroMqMessagePublisherFactory") ZeroMqMessagePublisherFactory bitcoinRawTxZeroMqMessagePublisherFactory
    ) {
        return new KmpTransactionPublisherFactory(bitcoinRawTxZeroMqMessagePublisherFactory);
    }

    @Bean(initMethod = "startAsync", destroyMethod = "stopAsync")
    @ConditionalOnMissingBean(value = Transaction.class, parameterizedContainer = MessagePublishService.class)
    @ConditionalOnBean(KmpTransactionPublisherFactory.class)
    public MessagePublishService<Transaction> bitcoinKmpTransactionPublishService(
            KmpTransactionPublisherFactory bitcoinKmpTransactionPublisherFactory
    ) {
        return new MessagePublishService<>(bitcoinKmpTransactionPublisherFactory);
    }
}

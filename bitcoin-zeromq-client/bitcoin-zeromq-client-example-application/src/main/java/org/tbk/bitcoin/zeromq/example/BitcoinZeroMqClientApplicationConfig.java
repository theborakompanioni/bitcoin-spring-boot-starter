package org.tbk.bitcoin.zeromq.example;

import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.params.MainNetParams;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.tbk.bitcoin.zeromq.bitcoinj.BitcoinjTransactionPublisherFactory;
import org.tbk.bitcoin.zeromq.client.BitcoinZeroMqTopics;
import org.tbk.bitcoin.zeromq.client.MessagePublishService;
import org.tbk.bitcoin.zeromq.client.ZeroMqMessagePublisherFactory;

@Slf4j
@Configuration
@EnableScheduling
public class BitcoinZeroMqClientApplicationConfig {

    @Bean("bitcoinRawTxZeroMqMessagePublisherFactory")
    public ZeroMqMessagePublisherFactory bitcoinRawTxZeroMqMessagePublisherFactory() {
        return ZeroMqMessagePublisherFactory.builder()
                .topic(BitcoinZeroMqTopics.rawtx())
                .address("tcp://localhost:28333")
                .build();
    }

    @Bean("bitcoinRawBlockZeroMqMessagePublisherFactory")
    public ZeroMqMessagePublisherFactory bitcoinRawBlockZeroMqMessagePublisherFactory() {
        return ZeroMqMessagePublisherFactory.builder()
                .topic(BitcoinZeroMqTopics.rawtx())
                .address("tcp://localhost:28333")
                .build();
    }

    @Bean("mainnetBitcoinjTransactionPublisherFactory")
    public BitcoinjTransactionPublisherFactory bitcoinjTransactionPublisherFactory(
            @Qualifier("bitcoinRawTxZeroMqMessagePublisherFactory")
                    ZeroMqMessagePublisherFactory bitcoinRawTxZeroMqMessagePublisherFactory
    ) {
        return new BitcoinjTransactionPublisherFactory(MainNetParams.get(), bitcoinRawTxZeroMqMessagePublisherFactory);
    }


    @Bean(name = "mainnetBitcoinjTransactionPublishService", initMethod = "startAsync", destroyMethod = "stopAsync")
    public MessagePublishService<Transaction> mainnetBitcoinjTransactionPublishService(
            @Qualifier("mainnetBitcoinjTransactionPublisherFactory")
                    BitcoinjTransactionPublisherFactory mainnetBitcoinjTransactionPublisherFactory
    ) {
        return new MessagePublishService<>(mainnetBitcoinjTransactionPublisherFactory);
    }
}

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

}

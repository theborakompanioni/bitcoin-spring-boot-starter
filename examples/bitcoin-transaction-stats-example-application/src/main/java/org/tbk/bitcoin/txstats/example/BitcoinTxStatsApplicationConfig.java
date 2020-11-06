package org.tbk.bitcoin.txstats.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.bitcoin.exchange.BitcoinStandardExchangeRateProvider;

@Slf4j
@Configuration
public class BitcoinTxStatsApplicationConfig {

    @Bean
    public BitcoinStandardExchangeRateProvider bitcoinStandardExchangeRateProvider() {
        return new BitcoinStandardExchangeRateProvider();
    }
}

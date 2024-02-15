package org.tbk.bitcoin.jsr354.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.tbk.bitcoin.exchange.BitcoinStandardExchangeRateProvider;

@Slf4j
@AutoConfiguration
public class BitcoinJsr354AutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(BitcoinStandardExchangeRateProvider.class)
    BitcoinStandardExchangeRateProvider bitcoinStandardExchangeRateProvider() {
        return new BitcoinStandardExchangeRateProvider();
    }

}

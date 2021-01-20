package org.tbk.bitcoin.jsr354.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.bitcoin.exchange.BitcoinStandardExchangeRateProvider;

@Slf4j
@Configuration(proxyBeanMethods = false)
public class BitcoinJsr354AutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(BitcoinStandardExchangeRateProvider.class)
    public BitcoinStandardExchangeRateProvider bitcoinStandardExchangeRateProvider() {
        return new BitcoinStandardExchangeRateProvider();
    }

}

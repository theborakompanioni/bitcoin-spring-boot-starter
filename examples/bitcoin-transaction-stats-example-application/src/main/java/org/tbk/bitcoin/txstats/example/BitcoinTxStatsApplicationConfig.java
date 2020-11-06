package org.tbk.bitcoin.txstats.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.tbk.bitcoin.exchange.BitcoinStandardExchangeRateProvider;
import org.tbk.xchange.jsr354.XChangeExchangeRateProvider;

import javax.money.Monetary;
import javax.money.convert.ConversionQueryBuilder;
import javax.money.convert.ExchangeRate;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Configuration
@EnableScheduling
public class BitcoinTxStatsApplicationConfig {

    @Bean
    public BitcoinStandardExchangeRateProvider bitcoinStandardExchangeRateProvider() {
        return new BitcoinStandardExchangeRateProvider();
    }
}

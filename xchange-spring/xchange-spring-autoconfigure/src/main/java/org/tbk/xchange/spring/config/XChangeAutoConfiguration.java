package org.tbk.xchange.spring.config;

import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.bitfinex.BitfinexExchange;
import org.knowm.xchange.bitstamp.BitstampExchange;
import org.knowm.xchange.bittrex.BittrexExchange;
import org.knowm.xchange.gemini.v1.GeminiExchange;
import org.knowm.xchange.kraken.KrakenExchange;
import org.knowm.xchange.therock.TheRockExchange;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConditionalOnClass(Exchange.class)
public class XChangeAutoConfiguration {

    @Configuration
    @ConditionalOnClass(BitstampExchange.class)
    public static class BitstampXChangeJsr354AutoConfiguration {
        @Bean
        @ConditionalOnMissingBean(BitstampExchange.class)
        public BitstampExchange bitstampExchange() {
            return ExchangeFactory.INSTANCE.createExchange(BitstampExchange.class);
        }
    }

    @Configuration
    @ConditionalOnClass(BitfinexExchange.class)
    public static class BitfinexXChangeJsr354AutoConfiguration {
        @Bean
        @ConditionalOnMissingBean(BitfinexExchange.class)
        public BitfinexExchange bitfinexExchange() {
            return ExchangeFactory.INSTANCE.createExchange(BitfinexExchange.class);
        }
    }

    @Configuration
    @ConditionalOnClass(BittrexExchange.class)
    public static class BittrexXChangeJsr354AutoConfiguration {
        @Bean
        @ConditionalOnMissingBean(BittrexExchange.class)
        public BittrexExchange bittrexExchange() {
            return ExchangeFactory.INSTANCE.createExchange(BittrexExchange.class);
        }
    }

    @Configuration
    @ConditionalOnClass(GeminiExchange.class)
    public static class GeminiXChangeJsr354AutoConfiguration {
        @Bean
        @ConditionalOnMissingBean(GeminiExchange.class)
        public GeminiExchange geminiExchange() {
            return ExchangeFactory.INSTANCE.createExchange(GeminiExchange.class);
        }
    }

    @Configuration
    @ConditionalOnClass(KrakenExchange.class)
    public static class KrakenXChangeJsr354AutoConfiguration {
        @Bean
        @ConditionalOnMissingBean(KrakenExchange.class)
        public KrakenExchange krakenExchange() {
            return ExchangeFactory.INSTANCE.createExchange(KrakenExchange.class);
        }
    }

    @Configuration
    @ConditionalOnClass(TheRockExchange.class)
    public static class TheRockXChangeJsr354AutoConfiguration {
        @Bean
        @ConditionalOnMissingBean(TheRockExchange.class)
        public TheRockExchange theRockExchange() {
            return ExchangeFactory.INSTANCE.createExchange(TheRockExchange.class);
        }
    }
}

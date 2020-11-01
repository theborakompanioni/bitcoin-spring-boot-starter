package org.tbk.xchange.jsr354.config;

import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
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
import org.tbk.xchange.jsr354.XChangeExchangeRateProvider;

import javax.money.convert.ProviderContext;
import javax.money.convert.ProviderContextBuilder;
import javax.money.convert.RateType;
import java.util.Optional;

@Slf4j
@Configuration
@ConditionalOnClass(Exchange.class)
public class XChangeJsr354AutoConfiguration {

    private static ProviderContext createCommonProviderContext(Exchange exchange) {
        String providerClassName = exchange.getClass().getSimpleName();
        String providerId = providerClassName
                .replace("Exchange", "")
                .toUpperCase();

        ExchangeSpecification specification = exchange.getDefaultExchangeSpecification();
        String exchangeName = Optional.ofNullable(specification.getExchangeName()).orElse(providerClassName);
        String exchangeDescription = Optional.ofNullable(specification.getExchangeDescription()).orElse("");

        return ProviderContextBuilder.of(providerId, RateType.DEFERRED)
                .set("providerName", exchangeName)
                .set("providerDescription", exchangeDescription)
                .build();
    }

    @Configuration
    @ConditionalOnClass(BitstampExchange.class)
    public static class BitstampXChangeJsr354AutoConfiguration {

        @Bean
        @ConditionalOnMissingBean(BitstampExchange.class)
        public BitstampExchange bitstampExchange() {
            return ExchangeFactory.INSTANCE.createExchange(BitstampExchange.class);
        }

        @Bean
        public XChangeExchangeRateProvider bitstampExchangeRateProvider(BitstampExchange exchange) {
            ProviderContext providerContext = createCommonProviderContext(exchange);
            return new XChangeExchangeRateProvider(providerContext, exchange);
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

        @Bean
        public XChangeExchangeRateProvider bitfinexExchangeRateProvider(BitfinexExchange exchange) {
            ProviderContext providerContext = createCommonProviderContext(exchange);
            return new XChangeExchangeRateProvider(providerContext, exchange);
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

        @Bean
        public XChangeExchangeRateProvider bittrexExchangeRateProvider(BittrexExchange exchange) {
            ProviderContext providerContext = createCommonProviderContext(exchange);
            return new XChangeExchangeRateProvider(providerContext, exchange);
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

        @Bean
        public XChangeExchangeRateProvider geminiExchangeRateProvider(GeminiExchange exchange) {
            ProviderContext providerContext = createCommonProviderContext(exchange);
            return new XChangeExchangeRateProvider(providerContext, exchange);
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

        @Bean
        public XChangeExchangeRateProvider krakenExchangeRateProvider(KrakenExchange exchange) {
            ProviderContext providerContext = createCommonProviderContext(exchange);
            return new XChangeExchangeRateProvider(providerContext, exchange);
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

        @Bean
        public XChangeExchangeRateProvider theRockExchangeRateProvider(TheRockExchange exchange) {
            ProviderContext providerContext = createCommonProviderContext(exchange);
            return new XChangeExchangeRateProvider(providerContext, exchange);
        }
    }
}

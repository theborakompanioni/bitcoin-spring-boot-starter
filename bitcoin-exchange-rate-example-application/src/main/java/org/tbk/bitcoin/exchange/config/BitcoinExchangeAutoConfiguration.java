package org.tbk.bitcoin.exchange.config;

import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.bitfinex.BitfinexExchange;
import org.knowm.xchange.bitstamp.BitstampExchange;
import org.knowm.xchange.bittrex.BittrexExchange;
import org.knowm.xchange.gemini.v1.GeminiExchange;
import org.knowm.xchange.kraken.KrakenExchange;
import org.knowm.xchange.therock.TheRockExchange;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.bitcoin.exchange.BitcoinStandardExchangeRateProvider;

import javax.money.Monetary;
import javax.money.convert.*;
import java.util.List;

@Slf4j
@Configuration
@EnableConfigurationProperties(BitcoinExchangeAutoConfigProperties.class)
// @ConditionalOnClass(MonetaryConversions.class)
// @ConditionalOnProperty(value = "org.tbk.bitcoin.enabled", havingValue = "true")
public class BitcoinExchangeAutoConfiguration {
    /*@NonNull
    private BitcoinExchangeAutoConfigProperties properties;

    public BitcoinExchangeAutoConfiguration(BitcoinExchangeAutoConfigProperties properties) {
        this.properties = properties;
    }*/

    @Bean
    public BitcoinStandardExchangeRateProvider bitcoinStandardExchangeRateProvider() {
        return new BitcoinStandardExchangeRateProvider();
    }


    @Bean
    @ConditionalOnClass(BitstampExchange.class)
    public XChangeExchangeRateProvider bitstampXChangeExchangeRateProvider(BitstampExchange exchange) {
        ProviderContext providerContext = ProviderContextBuilder.of("BITSTAMP", RateType.DEFERRED)
                .set("providerDescription", "BitstampExchange")
                .build();

        return new XChangeExchangeRateProvider(providerContext, exchange);
    }

    @Bean
    @ConditionalOnClass(BitstampExchange.class)
    @ConditionalOnMissingBean(BitstampExchange.class)
    public BitstampExchange bitstampExchange() {
        return ExchangeFactory.INSTANCE.createExchange(BitstampExchange.class);
    }

    @Bean
    @ConditionalOnClass(KrakenExchange.class)
    public XChangeExchangeRateProvider krakenXChangeExchangeRateProvider(KrakenExchange exchange) {
        ProviderContext providerContext = ProviderContextBuilder.of("KRAKEN", RateType.DEFERRED)
                .set("providerDescription", "KrakenExchange")
                .build();

        return new XChangeExchangeRateProvider(providerContext, exchange);
    }

    @Bean
    @ConditionalOnClass(KrakenExchange.class)
    @ConditionalOnMissingBean(KrakenExchange.class)
    public KrakenExchange krakenExchange() {
        return ExchangeFactory.INSTANCE.createExchange(KrakenExchange.class);
    }

    @Bean
    @ConditionalOnClass(BittrexExchange.class)
    // @ConditionalOnBean(CoinMarketCapExchange.class)
    public XChangeExchangeRateProvider bittrexXChangeExchangeRateProvider() {
        ProviderContext providerContext = ProviderContextBuilder.of("BITTREX", RateType.DEFERRED)
                .set("providerDescription", "BittrexExchange")
                .build();

        Exchange exchange = ExchangeFactory.INSTANCE.createExchange(BittrexExchange.class);
        return new XChangeExchangeRateProvider(providerContext, exchange);
    }

    @Bean
    @ConditionalOnClass(BitfinexExchange.class)
    // @ConditionalOnBean(CoinMarketCapExchange.class)
    public XChangeExchangeRateProvider bitfinexXChangeExchangeRateProvider() {
        ProviderContext providerContext = ProviderContextBuilder.of("BITFINEX", RateType.DEFERRED)
                .set("providerDescription", "BitfinexExchange")
                .build();

        Exchange exchange = ExchangeFactory.INSTANCE.createExchange(BitfinexExchange.class);
        return new XChangeExchangeRateProvider(providerContext, exchange);
    }

    @Bean
    @ConditionalOnClass(GeminiExchange.class)
    // @ConditionalOnBean(CoinMarketCapExchange.class)
    public XChangeExchangeRateProvider geminiXChangeExchangeRateProvider() {
        ProviderContext providerContext = ProviderContextBuilder.of("GEMINI", RateType.DEFERRED)
                .set("providerDescription", "GeminiExchange")
                .build();

        Exchange exchange = ExchangeFactory.INSTANCE.createExchange(GeminiExchange.class);
        return new XChangeExchangeRateProvider(providerContext, exchange);
    }

    @Bean
    @ConditionalOnClass(TheRockExchange.class)
    // @ConditionalOnBean(CoinMarketCapExchange.class)
    public XChangeExchangeRateProvider therockXChangeExchangeRateProvider() {
        ProviderContext providerContext = ProviderContextBuilder.of("THEROCK", RateType.DEFERRED)
                .set("providerDescription", "TheRockExchange")
                .build();

        Exchange exchange = ExchangeFactory.INSTANCE.createExchange(TheRockExchange.class);
        return new XChangeExchangeRateProvider(providerContext, exchange);
    }

    @Bean
    public CommandLineRunner test2(List<XChangeExchangeRateProvider> XChangeExchangeRateProviders) {
        return args -> {
            XChangeExchangeRateProviders.forEach(xChangeExchangeRateProvider -> {

                log.info("Test2: {}", xChangeExchangeRateProvider.getContext());

                ConversionQueryBuilder conversionQueryBuilder = ConversionQueryBuilder.of()
                        .setBaseCurrency(Monetary.getCurrency("BTC"))
                        .setTermCurrency(Monetary.getCurrency("USD"));

                final ExchangeRate exchangeRate = xChangeExchangeRateProvider.getExchangeRate(conversionQueryBuilder.build());

                log.info("exchangeRate: {}", exchangeRate);

            });

        };
    }
}

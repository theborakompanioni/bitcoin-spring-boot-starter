package org.tbk.xchange.jsr354.config;

import com.google.common.cache.CacheBuilderSpec;
import com.google.common.collect.ImmutableList;
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
import org.tbk.xchange.jsr354.CachingExchangeRateProvider;
import org.tbk.xchange.jsr354.XChangeExchangeRateProvider;
import org.tbk.xchange.jsr354.cache.ConversionQueryCache;
import org.tbk.xchange.jsr354.cache.ExchangeRateAvailabilityCache;
import org.tbk.xchange.jsr354.cache.ExchangeRateCache;

import javax.money.convert.ExchangeRateProvider;
import javax.money.convert.ProviderContext;

import static org.tbk.xchange.jsr354.MoreProviderContexts.createSimpleProviderContextBuilder;

@Slf4j
@Configuration
@ConditionalOnClass(Exchange.class)
public class XChangeJsr354AutoConfiguration {
    private static final CacheBuilderSpec exchangeRateAvailabilityCacheSpec = CacheBuilderSpec.parse(
            String.join(",", ImmutableList.<String>builder()
                    .add("maximumSize=10000")
                    .add("refreshInterval=10m")
                    .build())
    );

    private static final CacheBuilderSpec exchangeRateCacheSpec = CacheBuilderSpec.parse(
            String.join(",", ImmutableList.<String>builder()
                    .add("maximumSize=10000")
                    .add("refreshInterval=60s")
                    .build())
    );

    private static ExchangeRateCache createExchangeRateCache(ExchangeRateProvider provider) {
        return new ExchangeRateCache(ConversionQueryCache.builder(exchangeRateCacheSpec, provider));
    }

    private static ExchangeRateAvailabilityCache createExchangeRateAvailabilityCache(ExchangeRateProvider provider) {
        return new ExchangeRateAvailabilityCache(ConversionQueryCache.builder(exchangeRateAvailabilityCacheSpec, provider));
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
        public ExchangeRateProvider bitstampExchangeRateProvider(BitstampExchange exchange) {
            ExchangeRateProvider provider = bitstampExchangeRateProviderInner(exchange);

            ExchangeRateAvailabilityCache exchangeRateAvailabilityCache = createExchangeRateAvailabilityCache(provider);
            ExchangeRateCache exchangeRateCache = createExchangeRateCache(provider);

            ProviderContext context = provider.getContext();
            return new CachingExchangeRateProvider(context, exchangeRateAvailabilityCache, exchangeRateCache);
        }

        private XChangeExchangeRateProvider bitstampExchangeRateProviderInner(BitstampExchange exchange) {
            ProviderContext providerContext = createSimpleProviderContextBuilder(exchange).build();
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
        public ExchangeRateProvider bitfinexExchangeRateProvider(BitfinexExchange exchange) {
            ExchangeRateProvider provider = bitfinexExchangeRateProviderInner(exchange);

            ExchangeRateAvailabilityCache exchangeRateAvailabilityCache = createExchangeRateAvailabilityCache(provider);
            ExchangeRateCache exchangeRateCache = createExchangeRateCache(provider);

            ProviderContext context = provider.getContext();
            return new CachingExchangeRateProvider(context, exchangeRateAvailabilityCache, exchangeRateCache);
        }

        private XChangeExchangeRateProvider bitfinexExchangeRateProviderInner(BitfinexExchange exchange) {
            ProviderContext providerContext = createSimpleProviderContextBuilder(exchange).build();
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
        public ExchangeRateProvider bittrexExchangeRateProvider(BittrexExchange exchange) {
            ExchangeRateProvider provider = bittrexExchangeRateProviderInner(exchange);

            ExchangeRateAvailabilityCache exchangeRateAvailabilityCache = createExchangeRateAvailabilityCache(provider);
            ExchangeRateCache exchangeRateCache = createExchangeRateCache(provider);

            ProviderContext context = provider.getContext();
            return new CachingExchangeRateProvider(context, exchangeRateAvailabilityCache, exchangeRateCache);
        }

        private XChangeExchangeRateProvider bittrexExchangeRateProviderInner(BittrexExchange exchange) {
            ProviderContext providerContext = createSimpleProviderContextBuilder(exchange).build();
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
        public ExchangeRateProvider geminiExchangeRateProvider(GeminiExchange exchange) {
            ExchangeRateProvider provider = geminiExchangeRateProviderInner(exchange);

            ExchangeRateAvailabilityCache exchangeRateAvailabilityCache = createExchangeRateAvailabilityCache(provider);
            ExchangeRateCache exchangeRateCache = createExchangeRateCache(provider);

            ProviderContext context = provider.getContext();
            return new CachingExchangeRateProvider(context, exchangeRateAvailabilityCache, exchangeRateCache);
        }

        private XChangeExchangeRateProvider geminiExchangeRateProviderInner(GeminiExchange exchange) {
            ProviderContext providerContext = createSimpleProviderContextBuilder(exchange).build();
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
        public ExchangeRateProvider krakenExchangeRateProvider(KrakenExchange exchange) {
            ExchangeRateProvider provider = krakenExchangeRateProviderInner(exchange);

            ExchangeRateAvailabilityCache exchangeRateAvailabilityCache = createExchangeRateAvailabilityCache(provider);
            ExchangeRateCache exchangeRateCache = createExchangeRateCache(provider);

            ProviderContext context = provider.getContext();
            return new CachingExchangeRateProvider(context, exchangeRateAvailabilityCache, exchangeRateCache);
        }

        private XChangeExchangeRateProvider krakenExchangeRateProviderInner(KrakenExchange exchange) {
            ProviderContext providerContext = createSimpleProviderContextBuilder(exchange).build();
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
        public ExchangeRateProvider theRockExchangeRateProvider(TheRockExchange exchange) {
            ExchangeRateProvider provider = theRockExchangeRateProviderInner(exchange);

            ExchangeRateAvailabilityCache exchangeRateAvailabilityCache = createExchangeRateAvailabilityCache(provider);
            ExchangeRateCache exchangeRateCache = createExchangeRateCache(provider);

            ProviderContext context = provider.getContext();
            return new CachingExchangeRateProvider(context, exchangeRateAvailabilityCache, exchangeRateCache);
        }

        private XChangeExchangeRateProvider theRockExchangeRateProviderInner(TheRockExchange exchange) {
            ProviderContext providerContext = createSimpleProviderContextBuilder(exchange).build();
            return new XChangeExchangeRateProvider(providerContext, exchange);
        }
    }
}

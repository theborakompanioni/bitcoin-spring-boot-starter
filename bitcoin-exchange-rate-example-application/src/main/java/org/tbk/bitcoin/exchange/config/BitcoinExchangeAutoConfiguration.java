package org.tbk.bitcoin.exchange.config;

import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.bitstamp.BitstampExchange;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.bitcoin.exchange.BitcoinStandardExchangeRateProvider;

import javax.money.Monetary;
import javax.money.convert.*;

@Slf4j
@Configuration
// @EnableConfigurationProperties(BitcoinExchangeAutoConfigProperties.class)
// @ConditionalOnClass(MonetaryConversions.class)
// @ConditionalOnProperty(value = "org.tbk.bitcoin.enabled", havingValue = "true")
public class BitcoinExchangeAutoConfiguration {
    /*@NonNull
    private BitcoinExchangeAutoConfigProperties properties;

    public BitcoinExchangeAutoConfiguration(BitcoinExchangeAutoConfigProperties properties) {
        this.properties = properties;
    }*/

    @Bean
    public XChangeExchangeRateProvider xChangeExchangeRateProvider(BitstampExchange exchange) {
        ProviderContext providerContext = ProviderContextBuilder.of("BITSTAMP", RateType.DEFERRED)
                .set("providerDescription", "BitstampExchange")
                .build();

        return new XChangeExchangeRateProvider(providerContext, exchange);
    }

    @Bean
    public BitstampExchange bitstampExchange() {
        return ExchangeFactory.INSTANCE.createExchange(BitstampExchange.class);
    }

    @Bean
    public BitcoinStandardExchangeRateProvider bitcoinStandardExchangeRateProvider() {
        return new BitcoinStandardExchangeRateProvider();
    }

    @Bean
    public CommandLineRunner test2(XChangeExchangeRateProvider XChangeExchangeRateProvider) {
        return args -> {
            log.info("Test2:");

            ConversionQueryBuilder conversionQueryBuilder = ConversionQueryBuilder.of()
                    .setBaseCurrency(Monetary.getCurrency("BTC"))
                    .setTermCurrency(Monetary.getCurrency("USD"));

            final ExchangeRate exchangeRate = XChangeExchangeRateProvider.getExchangeRate(conversionQueryBuilder.build());

            log.info("exchangeRate: {}", exchangeRate);

        };
    }
}

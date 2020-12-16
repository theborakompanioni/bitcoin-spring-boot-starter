package org.tbk.bitcoin.duckdca.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.xchange.jsr354.XChangeExchangeRateProvider;

import javax.money.Monetary;
import javax.money.convert.ConversionQueryBuilder;
import javax.money.convert.ExchangeRate;
import java.util.List;

@Slf4j
@Configuration
public class BitcoinDuckDcaExampleApplicationConfig {
    @Bean
    public CommandLineRunner exchangeRateDemoRunner(List<XChangeExchangeRateProvider> XChangeExchangeRateProviders) {
        return args -> {
            if (XChangeExchangeRateProviders.isEmpty()) {
                log.warn("No XChangeExchangeRateProviders found.");
                return;
            }

            XChangeExchangeRateProviders.forEach(xChangeExchangeRateProvider -> {
                log.info("======================================================");
                log.info("Provider: {}", xChangeExchangeRateProvider.getContext());

                ConversionQueryBuilder conversionQueryBuilder = ConversionQueryBuilder.of()
                        .setBaseCurrency(Monetary.getCurrency("BTC"))
                        .setTermCurrency(Monetary.getCurrency("USD"));

                final ExchangeRate exchangeRate = xChangeExchangeRateProvider.getExchangeRate(conversionQueryBuilder.build());

                log.info("exchangeRate: {}", exchangeRate);
            });
            log.info("======================================================");
        };
    }
}

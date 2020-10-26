package org.tbk.bitcoin.exchange.example;

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
public class BitcoinExchangeApplicationConfig {

    @Bean
    public BitcoinStandardExchangeRateProvider bitcoinStandardExchangeRateProvider() {
        return new BitcoinStandardExchangeRateProvider();
    }

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

    @Bean
    @Profile({"debug"})
    public CommandLineRunner logBeanDefinitionNames(ApplicationContext ctx) {
        return args -> {
            log.info("Let's inspect the beans provided by Spring Boot:");

            String[] beanNames = ctx.getBeanDefinitionNames();
            Arrays.sort(beanNames);
            for (String beanName : beanNames) {
                log.info(beanName);
            }

        };
    }
}

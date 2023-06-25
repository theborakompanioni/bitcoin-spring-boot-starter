package org.tbk.bitcoin.exchange.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.boot.web.context.WebServerPortFileWriter;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import javax.money.Monetary;
import javax.money.convert.ConversionQuery;
import javax.money.convert.ConversionQueryBuilder;
import javax.money.convert.ExchangeRate;
import javax.money.convert.ExchangeRateProvider;
import java.util.List;

@Slf4j
@SpringBootApplication(proxyBeanMethods = false)
public class BitcoinExchangeRateExampleApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(BitcoinExchangeRateExampleApplication.class)
                .listeners(applicationPidFileWriter(), webServerPortFileWriter())
                .web(WebApplicationType.SERVLET)
                .profiles("development", "local")
                .run(args);
    }

    public static ApplicationListener<?> applicationPidFileWriter() {
        return new ApplicationPidFileWriter("application.pid");
    }

    public static ApplicationListener<?> webServerPortFileWriter() {
        return new WebServerPortFileWriter("application.port");
    }

    @Bean
    @Profile("!test")
    CommandLineRunner exchangeRateDemoRunner(List<ExchangeRateProvider> xChangeExchangeRateProviders) {
        return args -> {
            if (xChangeExchangeRateProviders.isEmpty()) {
                log.warn("No XChangeExchangeRateProviders found.");
                return;
            }

            ConversionQueryBuilder conversionQueryBuilder = ConversionQueryBuilder.of()
                    .setBaseCurrency(Monetary.getCurrency("BTC"))
                    .setTermCurrency(Monetary.getCurrency("USD"));

            ConversionQuery conversionQuery = conversionQueryBuilder.build();

            log.info("======================================================");
            log.info("ConversionQuery: {}", conversionQuery);
            log.info("Available provider count: {}", xChangeExchangeRateProviders.size());

            List<ExchangeRateProvider> eligibleProvider = xChangeExchangeRateProviders.stream()
                    .filter(it -> it.isAvailable(conversionQuery))
                    .toList();

            log.info("Eligible provider count: {}", eligibleProvider.size());

            eligibleProvider.forEach(xChangeExchangeRateProvider -> {
                log.info("------------------------------------------------------");
                log.info("Provider: {}", xChangeExchangeRateProvider.getContext());

                final ExchangeRate exchangeRate = xChangeExchangeRateProvider.getExchangeRate(conversionQuery);

                log.info("exchangeRate: {}", exchangeRate);
            });
            log.info("======================================================");
        };
    }
}

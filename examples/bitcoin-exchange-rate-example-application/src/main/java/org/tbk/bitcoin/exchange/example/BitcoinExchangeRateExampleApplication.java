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
import org.tbk.xchange.jsr354.XChangeExchangeRateProvider;

import javax.money.Monetary;
import javax.money.convert.ConversionQueryBuilder;
import javax.money.convert.ExchangeRate;
import java.util.List;

@Slf4j
@SpringBootApplication
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

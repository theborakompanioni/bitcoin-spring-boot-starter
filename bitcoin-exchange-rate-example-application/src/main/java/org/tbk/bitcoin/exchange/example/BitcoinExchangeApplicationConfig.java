package org.tbk.bitcoin.exchange.example;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.tbk.bitcoin.exchange.config.BitcoinExchangeAutoConfiguration;
import org.zalando.jackson.datatype.money.MoneyModule;

import java.util.Arrays;

@Slf4j
@Configuration
@EnableScheduling
@Import(BitcoinExchangeAutoConfiguration.class)
public class BitcoinExchangeApplicationConfig {

    @Bean
    public Jackson2ObjectMapperBuilder objectMapperBuilder() {
        MoneyModule moneyModule = new MoneyModule()
                .withDefaultFormatting()
                .withQuotedDecimalNumbers();

        return new Jackson2ObjectMapperBuilder()
                .modulesToInstall(moneyModule)
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .featuresToEnable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
                .indentOutput(true)
                .failOnUnknownProperties(false);
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

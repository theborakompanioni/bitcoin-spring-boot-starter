package org.tbk.bitcoin.exchange.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.tbk.bitcoin.exchange.config.BitcoinExchangeAutoConfiguration;

import java.util.Arrays;

@Slf4j
@Configuration
@EnableScheduling
@Import(BitcoinExchangeAutoConfiguration.class)
public class BitcoinExchangeApplicationConfig {

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

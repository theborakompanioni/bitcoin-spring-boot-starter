package org.tbk.bitcoin.exchange.config;

import lombok.NonNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.money.convert.ExchangeRateProvider;
import javax.money.convert.MonetaryConversions;

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
    // @ConditionalOnMissingBean
    public ExchangeRateProvider defaultRateProvider() {
        return MonetaryConversions.getExchangeRateProvider();
    }
}

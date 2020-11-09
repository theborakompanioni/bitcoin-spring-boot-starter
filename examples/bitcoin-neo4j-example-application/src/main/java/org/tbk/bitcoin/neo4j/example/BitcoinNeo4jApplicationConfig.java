package org.tbk.bitcoin.neo4j.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.tbk.bitcoin.exchange.BitcoinStandardExchangeRateProvider;
import org.tbk.spring.bitcoin.neo4j.model.BlockNeoEntity;

@Slf4j
@Configuration
public class BitcoinNeo4jApplicationConfig {

    @Bean
    public BitcoinStandardExchangeRateProvider bitcoinStandardExchangeRateProvider() {
        return new BitcoinStandardExchangeRateProvider();
    }
}

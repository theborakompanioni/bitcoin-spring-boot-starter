package org.tbk.bitcoin.txstats.example.score.cryptoscamdb;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.tbk.bitcoin.txstats.example.score.cryptoscamdb.client.CryptoScamDbClient;
import org.tbk.bitcoin.txstats.example.score.cryptoscamdb.client.CryptoScamDbClientImpl;

@Slf4j
@Configuration
public class CryptoScamDbConfig {
    private static final String DEFAULT_HOST = "api.cryptoscamdb.org";
    private static final String DEFAULT_ROOT_URI = "https://" + DEFAULT_HOST;

    @Bean
    public CryptoScamDbClientImpl cryptoScamDbClientImpl() {
        RestTemplate restTemplate = new RestTemplateBuilder()
                .rootUri(DEFAULT_ROOT_URI)
                .build();

        return new CryptoScamDbClientImpl(DEFAULT_ROOT_URI, restTemplate);
    }

    @Bean
    public CryptoScamDbServiceImpl CryptoScamDbServiceImpl(CryptoScamDbClient cryptoScamDbClient) {
        return new CryptoScamDbServiceImpl(cryptoScamDbClient);
    }

}

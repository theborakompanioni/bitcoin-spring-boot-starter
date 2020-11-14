package org.tbk.bitcoin.txstats.example.score.cryptoscamdb;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.bitcoin.tool.cryptoscamdb.client.CryptoScamDbClient;

@Slf4j
@Configuration
public class CryptoScamDbConfig {

    @Bean
    public CryptoScamDbServiceImpl cryptoScamDbServiceImpl(CryptoScamDbClient cryptoScamDbClient) {
        return new CryptoScamDbServiceImpl(cryptoScamDbClient);
    }

    @Bean
    public CryptoScamDbAddressScoreProvider cryptoScamDbAddressScoreProvider(CryptoScamDbService cryptoScamDbService) {
        return new CryptoScamDbAddressScoreProvider(cryptoScamDbService);
    }

}

package org.tbk.bitcoin.txstats.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.tbk.bitcoin.txstats.example.model.TxScoreNeoEntity;

@Slf4j
@Configuration
@EntityScan(basePackageClasses = TxScoreNeoEntity.class)
public class BitcoinTxStatsApplicationConfig {
}

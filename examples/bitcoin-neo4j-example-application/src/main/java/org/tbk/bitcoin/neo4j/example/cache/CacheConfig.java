package org.tbk.bitcoin.neo4j.example.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.bitcoin.jsonrpc.cache.CacheFacade;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

@Slf4j
@Configuration
public class CacheConfig {

    @Bean
    public CommandLineRunner cacheStatsRunner(CacheFacade caches) {
        return args -> {
            Flux.interval(Duration.ofSeconds(30), Schedulers.newSingle("chache-stats"))
                    .subscribe(foo -> {
                        log.info("======================================================");
                        log.info("tx: {}", caches.tx().stats());
                        log.info("txInfo: {}", caches.txInfo().stats());
                        log.info("block: {}", caches.block().stats());
                        log.info("blockInfo: {}", caches.blockInfo().stats());
                        log.info("======================================================");
                    });
        };
    }
}

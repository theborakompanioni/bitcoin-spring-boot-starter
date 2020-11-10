package org.tbk.bitcoin.txstats.example.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.bitcoin.jsonrpc.cache.CacheFacade;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import javax.money.convert.ConversionQuery;
import javax.money.convert.CurrencyConversion;
import javax.money.convert.MonetaryConversions;
import java.time.Duration;

@Slf4j
@Configuration
public class CacheConfig {

    @Bean
    public CommandLineRunner cacheStatsRunner(AppCacheFacade caches) {
        return args -> {
            Flux.interval(Duration.ofSeconds(30), Schedulers.newSingle("chache-stats"))
                    .subscribe(foo -> {
                        log.info("======================================================");
                        log.info("tx: {}", caches.tx().stats());
                        log.info("txInfo: {}", caches.txInfo().stats());
                        log.info("block: {}", caches.block().stats());
                        log.info("blockInfo: {}", caches.blockInfo().stats());
                        log.info("currencyConversion: {}", caches.currencyConversion().stats());
                        log.info("======================================================");
                    });
        };
    }

    @Bean
    public AppCacheFacade appCacheFacade(CacheFacade bitcoinJsonRpcCacheFacade,
                                         CurrencyConversionCache currencyConversionCache) {
        return AppCacheFacade.builder()
                .bitcoinJsonRpcCacheFacade(bitcoinJsonRpcCacheFacade)
                .currencyConversionCache(currencyConversionCache)
                .build();
    }

    @Bean
    public CurrencyConversionCache currencyConversionCache() {
        LoadingCache<ConversionQuery, CurrencyConversion> cache = CacheBuilder.newBuilder()
                .recordStats()
                .refreshAfterWrite(Duration.ofMinutes(1))
                .maximumSize(1_000)
                .build(new CacheLoader<>() {
                    @Override
                    public CurrencyConversion load(ConversionQuery key) {
                        return MonetaryConversions.getConversion(key);
                    }
                });
        return new CurrencyConversionCache(cache);
    }
}

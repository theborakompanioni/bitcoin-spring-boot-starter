package org.tbk.bitcoin.neo4j.example.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.msgilligan.bitcoinj.json.pojo.BlockInfo;
import com.msgilligan.bitcoinj.json.pojo.RawTransactionInfo;
import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import javax.money.convert.ConversionQuery;
import javax.money.convert.CurrencyConversion;
import javax.money.convert.MonetaryConversions;
import java.io.IOException;
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
                        log.info("blockInfo: {}", caches.blockInfo().stats());
                        log.info("currencyConversion: {}", caches.currencyConversion().stats());
                        log.info("======================================================");
                    });
        };
    }

    @Bean
    public CacheFacade cacheFacade(TransactionCache transactionCache,
                                   RawTransactionInfoCache RawTransactionInfoCache,
                                   BlockCache blockCache,
                                   BlockInfoCache blockInfoCache,
                                   CurrencyConversionCache currencyConversionCache) {
        return CacheFacade.builder()
                .transactionCache(transactionCache)
                .rawTransactionInfoCache(RawTransactionInfoCache)
                .blockCache(blockCache)
                .blockInfoCache(blockInfoCache)
                .currencyConversionCache(currencyConversionCache)
                .build();
    }

    @Bean
    public TransactionCache transactionCache(BitcoinClient bitcoinClient) {
        LoadingCache<Sha256Hash, Transaction> cache = CacheBuilder.newBuilder()
                .recordStats()
                .expireAfterAccess(Duration.ofMinutes(30))
                .maximumSize(10_000)
                .build(new CacheLoader<>() {
                    @Override
                    public Transaction load(Sha256Hash key) throws IOException {
                        return bitcoinClient.getRawTransaction(key);
                    }
                });
        return new TransactionCache(cache);
    }

    @Bean
    public RawTransactionInfoCache rawTransactionInfoCache(BitcoinClient bitcoinClient) {
        LoadingCache<Sha256Hash, RawTransactionInfo> cache = CacheBuilder.newBuilder()
                .recordStats()
                .expireAfterAccess(Duration.ofMinutes(30))
                .maximumSize(10_000)
                .build(new CacheLoader<>() {
                    @Override
                    public RawTransactionInfo load(Sha256Hash key) throws IOException {
                        return bitcoinClient.getRawTransactionInfo(key);
                    }
                });
        return new RawTransactionInfoCache(cache);
    }

    @Bean
    public BlockCache blockCache(BitcoinClient bitcoinClient) {
        LoadingCache<Sha256Hash, Block> cache = CacheBuilder.newBuilder()
                .recordStats()
                .expireAfterAccess(Duration.ofMinutes(30))
                .maximumSize(10_000)
                .build(new CacheLoader<>() {
                    @Override
                    public Block load(Sha256Hash key) throws IOException {
                        return bitcoinClient.getBlock(key);
                    }
                });
        return new BlockCache(cache);
    }

    @Bean
    public BlockInfoCache blockInfoCache(BitcoinClient bitcoinClient) {
        LoadingCache<Sha256Hash, BlockInfo> cache = CacheBuilder.newBuilder()
                .recordStats()
                .expireAfterAccess(Duration.ofMinutes(30))
                .maximumSize(10_000)
                .build(new CacheLoader<>() {
                    @Override
                    public BlockInfo load(Sha256Hash key) throws IOException {
                        return bitcoinClient.getBlockInfo(key);
                    }
                });
        return new BlockInfoCache(cache);
    }

    @Bean
    public CurrencyConversionCache conversionCache() {
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

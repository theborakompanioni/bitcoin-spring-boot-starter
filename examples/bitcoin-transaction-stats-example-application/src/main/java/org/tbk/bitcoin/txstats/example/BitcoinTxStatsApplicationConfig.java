package org.tbk.bitcoin.txstats.example;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.msgilligan.bitcoinj.json.pojo.BlockInfo;
import com.msgilligan.bitcoinj.json.pojo.RawTransactionInfo;
import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.bitcoin.exchange.BitcoinStandardExchangeRateProvider;
import org.tbk.bitcoin.txstats.example.cache.BlockInfoCache;
import org.tbk.bitcoin.txstats.example.cache.CurrencyConversionCache;
import org.tbk.bitcoin.txstats.example.cache.RawTransactionInfoCache;
import org.tbk.bitcoin.txstats.example.cache.TransactionCache;

import javax.money.convert.ConversionQuery;
import javax.money.convert.CurrencyConversion;
import javax.money.convert.MonetaryConversions;
import java.io.IOException;
import java.time.Duration;

@Slf4j
@Configuration
public class BitcoinTxStatsApplicationConfig {

    @Bean
    public BitcoinStandardExchangeRateProvider bitcoinStandardExchangeRateProvider() {
        return new BitcoinStandardExchangeRateProvider();
    }

    @Bean
    public TransactionCache transactionCache(BitcoinClient bitcoinClient) {
        LoadingCache<Sha256Hash, Transaction> cache = CacheBuilder.newBuilder()
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
    public BlockInfoCache blockInfoCache(BitcoinClient bitcoinClient) {
        LoadingCache<Sha256Hash, BlockInfo> cache = CacheBuilder.newBuilder()
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

    @Bean
    public CacheFacade cacheFacade(TransactionCache transactionCache,
                                   RawTransactionInfoCache RawTransactionInfoCache,
                                   BlockInfoCache blockInfoCache,
                                   CurrencyConversionCache currencyConversionCache) {
        return CacheFacade.builder()
                .transactionCache(transactionCache)
                .rawTransactionInfoCache(RawTransactionInfoCache)
                .blockInfoCache(blockInfoCache)
                .currencyConversionCache(currencyConversionCache)
                .build();
    }

    @Builder
    public static class CacheFacade {
        @NonNull
        private final TransactionCache transactionCache;

        @NonNull
        private final RawTransactionInfoCache rawTransactionInfoCache;

        @NonNull
        private final BlockInfoCache blockInfoCache;

        @NonNull
        private final CurrencyConversionCache currencyConversionCache;

        public TransactionCache tx() {
            return transactionCache;
        }

        public RawTransactionInfoCache txInfo() {
            return rawTransactionInfoCache;
        }

        public BlockInfoCache blockInfo() {
            return blockInfoCache;
        }

        public CurrencyConversionCache currencyConversion() {
            return currencyConversionCache;
        }
    }
}

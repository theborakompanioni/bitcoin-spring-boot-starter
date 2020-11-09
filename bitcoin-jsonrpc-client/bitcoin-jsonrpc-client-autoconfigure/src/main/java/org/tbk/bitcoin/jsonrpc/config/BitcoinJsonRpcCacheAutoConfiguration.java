package org.tbk.bitcoin.jsonrpc.config;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.msgilligan.bitcoinj.json.pojo.BlockInfo;
import com.msgilligan.bitcoinj.json.pojo.RawTransactionInfo;
import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import lombok.Builder;
import lombok.NonNull;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.bitcoin.jsonrpc.cache.*;

import java.io.IOException;
import java.time.Duration;

import static java.util.Objects.requireNonNull;

@Configuration
@EnableConfigurationProperties(BitcoinJsonRpcCacheAutoConfigProperties.class)
@ConditionalOnClass(CacheFacade.class)
@ConditionalOnProperty(value = "org.tbk.bitcoin.jsonrpc.cache.enabled", havingValue = "true", matchIfMissing = true)
public class BitcoinJsonRpcCacheAutoConfiguration {

    private BitcoinJsonRpcCacheAutoConfigProperties properties;

    public BitcoinJsonRpcCacheAutoConfiguration(BitcoinJsonRpcCacheAutoConfigProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Bean
    @ConditionalOnMissingBean(CacheFacade.class)
    public CacheFacade bitcoinJsonRpcCacheFacade(TransactionCache transactionCache,
                                                 RawTransactionInfoCache RawTransactionInfoCache,
                                                 BlockCache blockCache,
                                                 BlockInfoCache blockInfoCache) {
        return SimpleCacheFacade.builder()
                .transactionCache(transactionCache)
                .rawTransactionInfoCache(RawTransactionInfoCache)
                .blockCache(blockCache)
                .blockInfoCache(blockInfoCache)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(TransactionCache.class)
    public TransactionCache bitcoinJsonRpcTransactionCache(BitcoinClient bitcoinClient) {
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
    @ConditionalOnMissingBean(RawTransactionInfoCache.class)
    public RawTransactionInfoCache bitcoinJsonRpcRawTransactionInfoCache(BitcoinClient bitcoinClient) {
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
    @ConditionalOnMissingBean(BlockCache.class)
    public BlockCache bitcoinJsonRpcBlockCache(BitcoinClient bitcoinClient) {
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
    @ConditionalOnMissingBean(BlockInfoCache.class)
    public BlockInfoCache bitcoinJsonRpcInfoCache(BitcoinClient bitcoinClient) {
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

    @Builder
    public static class SimpleCacheFacade implements CacheFacade {
        @NonNull
        private final TransactionCache transactionCache;

        @NonNull
        private final RawTransactionInfoCache rawTransactionInfoCache;

        @NonNull
        private final BlockInfoCache blockInfoCache;

        @NonNull
        private final BlockCache blockCache;

        @Override
        public TransactionCache tx() {
            return transactionCache;
        }

        @Override
        public RawTransactionInfoCache txInfo() {
            return rawTransactionInfoCache;
        }

        @Override
        public BlockInfoCache blockInfo() {
            return blockInfoCache;
        }

        @Override
        public BlockCache block() {
            return blockCache;
        }
    }

}

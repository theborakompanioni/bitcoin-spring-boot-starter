package org.tbk.bitcoin.jsonrpc.config;

import com.google.common.cache.CacheBuilderSpec;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.util.Objects;

@ConfigurationProperties(
        prefix = "org.tbk.bitcoin.jsonrpc-cache",
        ignoreUnknownFields = false
)
@Getter
@AllArgsConstructor(onConstructor = @__(@ConstructorBinding))
public class BitcoinJsonRpcCacheAutoConfigProperties {
    private static final CacheBuilderSpec defaultTransactionCacheSpec = CacheBuilderSpec.parse("""
            recordStats,maximumSize=10000,expireAfterAccess=30m
            """);
    private static final CacheBuilderSpec defaultRawTransactionInfoCacheSpec = CacheBuilderSpec.parse("""
            recordStats,maximumSize=10000,expireAfterAccess=30m
            """);
    private static final CacheBuilderSpec defaultBlockCacheSpec = CacheBuilderSpec.parse("""
            recordStats,maximumSize=100,expireAfterAccess=30m
            """);
    private static final CacheBuilderSpec defaultBlockInfoCacheSpec = CacheBuilderSpec.parse("""
            recordStats,maximumSize=1000,expireAfterAccess=30m
            """);

    private boolean enabled = true;

    private CacheBuilderSpecOption transaction;
    private CacheBuilderSpecOption rawTransactionInfo;
    private CacheBuilderSpecOption block;
    private CacheBuilderSpecOption blockInfo;

    public CacheBuilderSpecOption getTransaction() {
        return Objects.requireNonNullElseGet(transaction, () -> new CacheBuilderSpecOption(true, defaultTransactionCacheSpec.toParsableString()));
    }

    public CacheBuilderSpecOption getRawTransactionInfo() {
        return Objects.requireNonNullElseGet(rawTransactionInfo, () -> new CacheBuilderSpecOption(true, defaultRawTransactionInfoCacheSpec.toParsableString()));

    }

    public CacheBuilderSpecOption getBlock() {
        return Objects.requireNonNullElseGet(block, () -> new CacheBuilderSpecOption(true, defaultBlockCacheSpec.toParsableString()));

    }

    public CacheBuilderSpecOption getBlockInfo() {
        return Objects.requireNonNullElseGet(blockInfo, () -> new CacheBuilderSpecOption(true, defaultBlockInfoCacheSpec.toParsableString()));
    }

    @Getter
    @AllArgsConstructor(onConstructor = @__(@ConstructorBinding))
    public static class CacheBuilderSpecOption {

        boolean enabled = true;

        String specification = "";

        public CacheBuilderSpec getCacheBuilderSpec() {
            return enabled && specification != null ? CacheBuilderSpec.parse(specification) : CacheBuilderSpec.disableCaching();
        }
    }
}

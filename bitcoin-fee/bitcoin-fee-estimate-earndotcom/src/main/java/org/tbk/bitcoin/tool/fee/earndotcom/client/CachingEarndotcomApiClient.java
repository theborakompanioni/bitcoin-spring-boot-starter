package org.tbk.bitcoin.tool.fee.earndotcom.client;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheBuilderSpec;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.Builder;
import org.tbk.bitcoin.tool.fee.earndotcom.client.proto.RecommendedTransactionFees;
import org.tbk.bitcoin.tool.fee.earndotcom.client.proto.TransactionFeesSummary;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.util.Objects.requireNonNull;

public final class CachingEarndotcomApiClient implements EarndotcomApiClient {
    private static final String SINGLE_CACHE_KEY_VALUE = "*";

    private static final CacheBuilderSpec defaultFeesListCacheBuilderSpec = CacheBuilderSpec.parse("");
    private static final CacheBuilderSpec defaultFeesRecommendedCacheBuilderSpec = CacheBuilderSpec.parse("");

    private final EarndotcomApiClient client;

    private final LoadingCache<String, TransactionFeesSummary> feesListCache;

    private final LoadingCache<String, RecommendedTransactionFees> feesRecommendedCache;

    @Builder
    private CachingEarndotcomApiClient(EarndotcomApiClient delegate,
                                       CacheBuilderSpec feesListCacheBuilderSpec,
                                       CacheBuilderSpec feesRecommendedCacheBuilderSpec) {
        this.client = requireNonNull(delegate);

        this.feesListCache = CacheBuilder.from(firstNonNull(feesListCacheBuilderSpec, defaultFeesListCacheBuilderSpec))
                .build(CacheLoader.from((key) -> client.transactionFeesSummary()));

        this.feesRecommendedCache = CacheBuilder.from(firstNonNull(feesRecommendedCacheBuilderSpec, defaultFeesRecommendedCacheBuilderSpec))
                .build(CacheLoader.from((key) -> client.recommendedTransactionFees()));
    }

    @Override
    public RecommendedTransactionFees recommendedTransactionFees() {
        return feesRecommendedCache.getUnchecked(SINGLE_CACHE_KEY_VALUE);
    }

    @Override
    public TransactionFeesSummary transactionFeesSummary() {
        return feesListCache.getUnchecked(SINGLE_CACHE_KEY_VALUE);
    }
}

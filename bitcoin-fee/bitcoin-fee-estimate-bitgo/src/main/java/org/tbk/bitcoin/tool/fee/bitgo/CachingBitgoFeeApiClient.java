package org.tbk.bitcoin.tool.fee.bitgo;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheBuilderSpec;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.Builder;
import org.tbk.bitcoin.tool.fee.bitgo.proto.BtcTxFeeRequest;
import org.tbk.bitcoin.tool.fee.bitgo.proto.BtcTxFeeResponse;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.util.Objects.requireNonNull;

public final class CachingBitgoFeeApiClient implements BitgoFeeApiClient {
    private static final CacheBuilderSpec defaultResponseCacheBuilderSpec = CacheBuilderSpec.parse("");

    private final LoadingCache<BtcTxFeeRequest, BtcTxFeeResponse> responseCache;

    @Builder
    private CachingBitgoFeeApiClient(BitgoFeeApiClient delegate,
                                     CacheBuilderSpec responseCacheBuilderSpec) {
        requireNonNull(delegate);

        this.responseCache = CacheBuilder.from(firstNonNull(responseCacheBuilderSpec, defaultResponseCacheBuilderSpec))
                .build(CacheLoader.from(delegate::btcTxFee));
    }

    @Override
    public BtcTxFeeResponse btcTxFee(BtcTxFeeRequest request) {
        return this.responseCache.getUnchecked(request);
    }
}

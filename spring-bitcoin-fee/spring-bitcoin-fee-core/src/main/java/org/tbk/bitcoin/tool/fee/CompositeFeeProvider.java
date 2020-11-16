package org.tbk.bitcoin.tool.fee;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import reactor.core.publisher.Flux;

import java.util.List;

import static java.util.Objects.requireNonNull;

public final class CompositeFeeProvider extends AbstractFeeProvider {

    private final List<FeeProvider> feeProviders;

    public CompositeFeeProvider(List<FeeProvider> feeProviders) {
        this.feeProviders = ImmutableList.copyOf(requireNonNull(feeProviders));
    }

    @Override
    public boolean supports(FeeRecommendationRequest request) {
        return this.feeProviders.stream().anyMatch(provider -> provider.supports(request));
    }

    @Override
    protected Flux<FeeRecommendationResponse> requestHook(FeeRecommendationRequest request) {
        return Flux.fromIterable(feeProviders)
                .filter(provider -> provider.supports(request))
                .flatMap(provider -> provider.request(request));
    }

    @VisibleForTesting
    int getProviderCount() {
        return feeProviders.size();
    }
}

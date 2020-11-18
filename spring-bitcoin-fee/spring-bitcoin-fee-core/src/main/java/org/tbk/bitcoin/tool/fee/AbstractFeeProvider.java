package org.tbk.bitcoin.tool.fee;

import reactor.core.publisher.Flux;

import java.util.Optional;

public abstract class AbstractFeeProvider implements FeeProvider {

    private final ProviderInfo providerInfo;

    protected AbstractFeeProvider() {
        this(null);
    }

    protected AbstractFeeProvider(ProviderInfo providerInfo) {
        this.providerInfo = providerInfo;
    }

    public final Flux<FeeRecommendationResponse> request(FeeRecommendationRequest feeRecommendationRequest) {
        return Flux.just(feeRecommendationRequest)
                .filter(this::supports)
                .flatMap(this::requestHook)
                .flatMap(this::transformHook)
                .map(val -> FeeRecommendationResponseImpl.builder()
                        .feeRecommendations(val.getFeeRecommendations())
                        .providerInfo(Optional.ofNullable(val.getProviderInfo()).orElse(providerInfo))
                        .build()
                );
    }

    // TODO: make this method return FeeRecommendations only
    protected abstract Flux<FeeRecommendationResponse> requestHook(FeeRecommendationRequest request);

    protected Flux<FeeRecommendationResponse> transformHook(FeeRecommendationResponse result) {
        return Flux.just(result);
    }
}

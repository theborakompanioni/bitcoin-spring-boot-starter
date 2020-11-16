package org.tbk.bitcoin.tool.fee;

import reactor.core.publisher.Flux;

public abstract class AbstractFeeProvider implements FeeProvider {

    public final Flux<FeeRecommendationResponse> request(FeeRecommendationRequest feeRecommendationRequest) {
        return Flux.just(feeRecommendationRequest)
                .filter(this::supports)
                .flatMap(this::requestHook)
                .flatMap(this::transformHook);
    }

    protected abstract Flux<FeeRecommendationResponse> requestHook(FeeRecommendationRequest request);

    protected Flux<FeeRecommendationResponse> transformHook(FeeRecommendationResponse result) {
        return Flux.just(result);
    }
}

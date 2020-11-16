package org.tbk.bitcoin.tool.fee;

import reactor.core.publisher.Flux;

public interface FeeProvider {

    default boolean supports(FeeRecommendationRequest request) {
        return true;
    }

    Flux<FeeRecommendationResponse> request(FeeRecommendationRequest request);

}

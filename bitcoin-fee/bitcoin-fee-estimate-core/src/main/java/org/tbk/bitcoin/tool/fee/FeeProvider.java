package org.tbk.bitcoin.tool.fee;

import reactor.core.publisher.Flux;

public interface FeeProvider {

    boolean supports(FeeRecommendationRequest request);

    Flux<FeeRecommendationResponse> request(FeeRecommendationRequest request);

}

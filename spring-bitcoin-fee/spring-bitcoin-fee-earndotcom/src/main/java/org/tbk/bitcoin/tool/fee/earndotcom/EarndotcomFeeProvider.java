package org.tbk.bitcoin.tool.fee.earndotcom;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.tbk.bitcoin.tool.fee.FeeProvider;
import org.tbk.bitcoin.tool.fee.FeeRecommendationRequest;
import org.tbk.bitcoin.tool.fee.FeeRecommendationResponse;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
public class EarndotcomFeeProvider implements FeeProvider {

    @NonNull
    private final EarndotcomApiClient client;

    @Override
    public Flux<FeeRecommendationResponse> request(FeeRecommendationRequest request) {
        return Flux.empty();
    }
}

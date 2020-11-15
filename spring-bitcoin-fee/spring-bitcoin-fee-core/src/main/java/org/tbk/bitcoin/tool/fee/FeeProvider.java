package org.tbk.bitcoin.tool.fee;

public interface FeeProvider {

    FeeRecommendationResponse request(FeeRecommendationRequest request);

}

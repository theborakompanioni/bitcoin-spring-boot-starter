package org.tbk.bitcoin.tool.fee.jsonrpc;

import lombok.RequiredArgsConstructor;
import org.tbk.bitcoin.tool.fee.AbstractFeeProvider;
import org.tbk.bitcoin.tool.fee.FeeRecommendationRequest;
import org.tbk.bitcoin.tool.fee.FeeRecommendationResponse;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
public class BitcoinJsonRpcFeeProvider extends AbstractFeeProvider {
    @Override
    public boolean supports(FeeRecommendationRequest request) {
        return request.getDesiredConfidence().isEmpty();
    }

    @Override
    protected Flux<FeeRecommendationResponse> requestHook(FeeRecommendationRequest request) {
        return null;
    }
}

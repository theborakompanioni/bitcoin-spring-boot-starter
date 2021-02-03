package org.tbk.bitcoin.tool.fee.jsonrpc;

import org.tbk.bitcoin.tool.fee.AbstractFeeProvider;
import org.tbk.bitcoin.tool.fee.FeeRecommendationRequest;
import org.tbk.bitcoin.tool.fee.FeeRecommendationResponse;
import org.tbk.bitcoin.tool.fee.ProviderInfo;
import reactor.core.publisher.Flux;


public class BitcoinJsonRpcFeeProvider extends AbstractFeeProvider {

    private static final ProviderInfo providerInfo = ProviderInfo.SimpleProviderInfo.builder()
            .name("bitcoin-jsonrpc-estimatesmartfee")
            .description("")
            .build();

    protected BitcoinJsonRpcFeeProvider() {
        super(providerInfo);
    }

    @Override
    public boolean supports(FeeRecommendationRequest request) {
        return request.getDesiredConfidence().isEmpty();
    }

    @Override
    protected Flux<FeeRecommendationResponse> requestHook(FeeRecommendationRequest request) {
        return null;
    }
}

package org.tbk.bitcoin.tool.fee.bitgo;

import lombok.RequiredArgsConstructor;
import org.tbk.bitcoin.tool.fee.AbstractFeeProvider;
import org.tbk.bitcoin.tool.fee.FeeRecommendationRequest;
import org.tbk.bitcoin.tool.fee.FeeRecommendationResponse;
import org.tbk.bitcoin.tool.fee.FeeRecommendationResponseImpl;
import org.tbk.bitcoin.tool.fee.FeeRecommendationResponseImpl.SatPerVbyteImpl;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
public class BitgoFeeProvider extends AbstractFeeProvider {

    private final BitgoFeeApiClient client;

    @Override
    public boolean supports(FeeRecommendationRequest request) {
        // while bitgo returns a "confidence" property, it does not support it as request param
        return request.getDesiredConfidence().isEmpty();
    }

    @Override
    protected Flux<FeeRecommendationResponse> requestHook(FeeRecommendationRequest request) {
        BtcTxFeeRequest apiRequest = buildApiRequest(request);
        BtcTxFeeResponse response = client.btcTxFee(apiRequest);

        long satPerKilobyte = response.getFeePerKb();
        SatPerVbyteImpl satPerVbyte = SatPerVbyteImpl.fromSatPerKilobyte(satPerKilobyte);

        return Flux.just(FeeRecommendationResponseImpl.builder()
                .addFeeRecommendation(FeeRecommendationResponseImpl.FeeRecommendationImpl.builder()
                        .satPerVbyte(satPerVbyte)
                        .build())
                .build());
    }

    private BtcTxFeeRequest buildApiRequest(FeeRecommendationRequest request) {
        BtcTxFeeRequest.Builder apiRequestBuilder = BtcTxFeeRequest.newBuilder();
        request.getBlockTarget().ifPresent(apiRequestBuilder::setNumBlocks);
        return apiRequestBuilder.build();
    }
}

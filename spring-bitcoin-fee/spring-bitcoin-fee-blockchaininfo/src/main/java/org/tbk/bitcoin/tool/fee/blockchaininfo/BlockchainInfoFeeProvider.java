package org.tbk.bitcoin.tool.fee.blockchaininfo;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.tbk.bitcoin.tool.fee.AbstractFeeProvider;
import org.tbk.bitcoin.tool.fee.FeeRecommendationRequest;
import org.tbk.bitcoin.tool.fee.FeeRecommendationResponse;
import org.tbk.bitcoin.tool.fee.FeeRecommendationResponseImpl;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
public class BlockchainInfoFeeProvider extends AbstractFeeProvider {
    @NonNull
    private final BlockchainInfoFeeApiClient client;

    @Override
    public boolean supports(FeeRecommendationRequest request) {
        // blockchain.info fees do not support any customized request
        return request.getDesiredConfidence().isEmpty() &&
                request.getDurationTarget().isEmpty();
    }

    @Override
    public Flux<FeeRecommendationResponse> requestHook(FeeRecommendationRequest request) {
        MempoolFees mempoolFees = client.mempoolFees();

        // get the standard fee from the response
        // as the "priority" value has no other metadata available
        // it is simply unknown what to expect from it or what it exactly means.
        long satsPerByte = mempoolFees.getRegular();

        return Flux.just(FeeRecommendationResponseImpl.builder()
                .addFeeRecommendation(FeeRecommendationResponseImpl.FeeRecommendationImpl.builder()
                        .satPerVbyte(FeeRecommendationResponseImpl.SatPerVbyteImpl.builder()
                                .satPerVbyteValue(satsPerByte)
                                .build())
                        .build())
                .build());
    }
}

package org.tbk.bitcoin.tool.fee.blockchaininfo;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.tbk.bitcoin.tool.fee.FeeProvider;
import org.tbk.bitcoin.tool.fee.FeeRecommendationRequest;
import org.tbk.bitcoin.tool.fee.FeeRecommendationRequest.Urgency;
import org.tbk.bitcoin.tool.fee.FeeRecommendationResponse;
import org.tbk.bitcoin.tool.fee.SatoshiPerVbyte;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
public class BlockchainInfoFeeProvider implements FeeProvider {
    @NonNull
    private final BlockchainInfoFeeApiClient client;

    @Override
    public boolean supports(FeeRecommendationRequest request) {
        boolean isUrgencySpecifiedInBlocks = request.getUrgencyCase() == FeeRecommendationRequest.UrgencyCase.URGENCY_BLOCKS;

        // blockchain.info data does not support per block fee estimations
        return !isUrgencySpecifiedInBlocks;
    }

    @Override
    public Flux<FeeRecommendationResponse> request(FeeRecommendationRequest feeRecommendationRequest) {
        return Flux.just(feeRecommendationRequest)
                .filter(this::supports)
                .flatMap(request -> {
                    MempoolFees mempoolFees = client.mempoolFees();

                    long satsPerByte = extractResult(request, mempoolFees);

                    return Flux.just(FeeRecommendationResponse.newBuilder()
                            .setRequest(request)
                            .addRecommendation(FeeRecommendationResponse.FeeRecommendation.newBuilder()
                                    .setSatPerVbyte(SatoshiPerVbyte.newBuilder()
                                            .setSatPerVbyte(satsPerByte)
                                            .build())
                                    .build())
                            .build());
                });
    }

    private static long extractResult(FeeRecommendationRequest request, MempoolFees data) {
        switch (request.getUrgencyCase()) {
            case URGENCY_BLOCKS:
                throw new IllegalStateException("blockchain.info data does not support per block fee estimations");
            case URGENCY_TYPE:
                return extractResultByUrgency(request.getUrgencyType(), data);
        }

        return extractResultByUrgency(Urgency.DEFAULT, data);
    }

    private static long extractResultByUrgency(Urgency urgency, MempoolFees data) {
        switch (urgency) {
            case MIN:
                return data.getLimit().getMin();
            case LOW:
                // average of "min" and "regular"
                return Math.max(data.getLimit().getMin(),
                        Math.min((data.getRegular() + data.getLimit().getMin() / 2), data.getRegular()));
            case MEDIUM:
                return data.getRegular();
            case HIGH:
                return data.getPriority();
            case HAX:
                return data.getLimit().getMax();
            case DEFAULT: {
                // average of "regular" and "priority"
                return Math.max(data.getRegular(),
                        Math.min((data.getRegular() + data.getPriority() / 2), data.getPriority()));
            }
            case UNRECOGNIZED:
            default:
                return data.getRegular();
        }
    }
}

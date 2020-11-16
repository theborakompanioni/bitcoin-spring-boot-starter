package org.tbk.bitcoin.tool.fee.bitcoinerlive;

import com.google.common.collect.ImmutableMap;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.tbk.bitcoin.tool.fee.*;
import org.tbk.bitcoin.tool.fee.FeeRecommendationRequest.Urgency;
import org.tbk.bitcoin.tool.fee.bitcoinerlive.FeeEstimatesLatestResponse.Estimate;
import org.tbk.bitcoin.tool.fee.util.MoreBitcoin;
import org.tbk.bitcoin.tool.fee.util.MoreUrgency;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class BitcoinerliveFeeProvider implements FeeProvider {
    private static final Map<Confidence, FeeEstimatesLatestRequest.Confidence> confidenceMapping = ImmutableMap
            .<Confidence, FeeEstimatesLatestRequest.Confidence>builder()
            .put(Confidence.DEFAULT, FeeEstimatesLatestRequest.Confidence.HIGH)
            .put(Confidence.MIN, FeeEstimatesLatestRequest.Confidence.LOW)
            .put(Confidence.LOW, FeeEstimatesLatestRequest.Confidence.LOW)
            .put(Confidence.MEDIUM, FeeEstimatesLatestRequest.Confidence.MEDIUM)
            .put(Confidence.HIGH, FeeEstimatesLatestRequest.Confidence.HIGH)
            .put(Confidence.HAX, FeeEstimatesLatestRequest.Confidence.HIGH)
            .put(Confidence.UNRECOGNIZED, FeeEstimatesLatestRequest.Confidence.HIGH)
            .build();

    @NonNull
    private final BitcoinerliveFeeApiClient client;

    @Override
    public Flux<FeeRecommendationResponse> request(FeeRecommendationRequest feeRecommendationRequest) {
        return Flux.just(feeRecommendationRequest)
                .filter(this::supports)
                .flatMap(request -> {
                    FeeEstimatesLatestRequest build = toApiRequest(request);
                    FeeEstimatesLatestResponse response = client.feeEstimatesLatest(build);

                    long minutes = toDuration(request).toMinutes();

                    Optional<Estimate> estimateOrEmpty = response.getEstimateMap().entrySet().stream()
                            .filter(val -> Long.valueOf(val.getKey(), 10) <= minutes)
                            .map(Map.Entry::getValue)
                            .findFirst();

                    if (estimateOrEmpty.isEmpty()) {
                        return Flux.empty();
                    }

                    Estimate estimate = estimateOrEmpty.orElseThrow();

                    return Flux.just(FeeRecommendationResponse.newBuilder()
                            .setRequest(request)
                            .addRecommendation(FeeRecommendationResponse.FeeRecommendation.newBuilder()
                                    .setSatPerVbyte(SatoshiPerVbyte.newBuilder()
                                            .setSatPerVbyte(estimate.getSatPerVbyte())
                                            .build())
                                    .build())
                            .build());
                });
    }

    private FeeEstimatesLatestRequest toApiRequest(FeeRecommendationRequest request) {
        return FeeEstimatesLatestRequest.newBuilder()
                .setConfidenceType(toConfidenceType(request))
                .build();
    }

    private Duration toDuration(FeeRecommendationRequest request) {
        switch (request.getUrgencyCase()) {
            case URGENCY_TYPE:
                return MoreUrgency.urgencyToDuration(request.getUrgencyType());
            case URGENCY_BLOCKS:
                return MoreBitcoin.averageBlockDuration().multipliedBy(request.getUrgencyBlocks());
        }

        return MoreUrgency.urgencyToDuration(Urgency.DEFAULT);
    }

    private FeeEstimatesLatestRequest.Confidence toConfidenceType(FeeRecommendationRequest request) {
        switch (request.getConfidenceCase()) {
            case CONFIDENCE_TYPE:
                return confidenceMapping.get(request.getConfidenceType());
            case CONFIDENCE_VAL: {
                if (request.getConfidenceVal() <= 50L) {
                    return FeeEstimatesLatestRequest.Confidence.LOW;
                } else if (request.getConfidenceVal() <= 80L) {
                    return FeeEstimatesLatestRequest.Confidence.LOW;
                }
                return FeeEstimatesLatestRequest.Confidence.HIGH;
            }
        }

        return confidenceMapping.get(Confidence.DEFAULT);
    }
}

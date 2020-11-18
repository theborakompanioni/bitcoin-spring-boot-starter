package org.tbk.bitcoin.tool.fee.bitcoinerlive;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.tbk.bitcoin.tool.fee.*;
import org.tbk.bitcoin.tool.fee.FeeRecommendationResponseImpl.FeeRecommendationImpl;
import org.tbk.bitcoin.tool.fee.FeeRecommendationResponseImpl.SatPerVbyteImpl;
import org.tbk.bitcoin.tool.fee.bitcoinerlive.FeeEstimatesLatestRequest.Confidence;
import org.tbk.bitcoin.tool.fee.bitcoinerlive.FeeEstimatesLatestResponse.Estimate;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Slf4j
public class BitcoinerliveFeeProvider extends AbstractFeeProvider {

    private static final ProviderInfo providerInfo = ProviderInfo.SimpleProviderInfo.builder()
            .name("Bitcoinerlive")
            .description("")
            .build();

    private static final Map<Double, Confidence> confidenceMap = ImmutableMap.<Double, Confidence>builder()
            .put(Double.valueOf("0.5"), Confidence.LOW)
            .put(Double.valueOf("0.8"), Confidence.MEDIUM)
            .put(Double.valueOf("0.9"), Confidence.HIGH)
            .build();

    private static double highestConfidenceKey = confidenceMap.keySet().stream()
            .mapToDouble(k -> k)
            .min()
            .orElseThrow();

    private final BitcoinerliveFeeApiClient client;

    public BitcoinerliveFeeProvider(BitcoinerliveFeeApiClient client) {
        super(providerInfo);

        this.client = requireNonNull(client);
    }

    @Override
    public boolean supports(FeeRecommendationRequest request) {
        return true;
    }

    @Override
    public Flux<FeeRecommendationResponse> requestHook(FeeRecommendationRequest feeRecommendationRequest) {
        FeeEstimatesLatestRequest request = toApiRequest(feeRecommendationRequest);
        FeeEstimatesLatestResponse response = client.feeEstimatesLatest(request);

        log.debug("data: {}", response);

        Map<String, Estimate> estimateMap = response.getEstimateMap();
        if (estimateMap.isEmpty()) {
            log.warn("no estimation entries present in response for request: {}", feeRecommendationRequest);
            return Flux.empty();
        }

        long minutes = feeRecommendationRequest.getDurationTarget()
                .map(Duration::toMinutes)
                .orElse(0L);

        Map<Long, Estimate> estimateMapWithMinutesAsKeys = estimateMap.entrySet().stream()
                .collect(Collectors.toMap(val -> Long.valueOf(val.getKey(), 10), Map.Entry::getValue));

        // take the maximum duration that is lower or equal to the duration specified
        OptionalLong minutesKeyOrEmpty = estimateMapWithMinutesAsKeys.keySet().stream()
                .mapToLong(val -> val)
                .filter(val -> val <= minutes)
                .max();

        long minutesKey = minutesKeyOrEmpty.orElseGet(() -> {
            // if the key is not present, the user must have specified a value smaller
            // than the lowest minute value present in the map.
            // in this case, take the lowest value and continue.
            OptionalLong lowestMinutesKey = estimateMapWithMinutesAsKeys.keySet().stream()
                    .mapToLong(val -> val)
                    .min();

            return lowestMinutesKey.orElseThrow(() -> new IllegalStateException("Cannot find lowest duration key"));
        });

        // the map is guaranteed to have entries (empty maps return early)
        Estimate estimate = estimateMapWithMinutesAsKeys.get(minutesKey);

        return Flux.just(FeeRecommendationResponseImpl.builder()
                .addFeeRecommendation(FeeRecommendationImpl.builder()
                        .satPerVbyte(SatPerVbyteImpl.builder()
                                .satPerVbyteValue(estimate.getSatPerVbyte())
                                .build())
                        .build())
                .build());
    }

    private FeeEstimatesLatestRequest toApiRequest(FeeRecommendationRequest request) {
        FeeEstimatesLatestRequest.Builder builder = FeeEstimatesLatestRequest.newBuilder();

        toConfidenceType(request).ifPresent(builder::setConfidenceType);

        return builder.build();
    }

    /**
     * Extracts the confidence value from the request and maps it to
     * a value the api understands. If it is higher than the highest supported value,
     * then this implementation wont simply fail, but use the highest default value.
     * <p>
     * This may or may not what the user wants or expect and is subject to change.
     * As this api provides values with up to 90% confidence, it might be safe to do so.
     */
    private Optional<Confidence> toConfidenceType(FeeRecommendationRequest request) {
        return request.getDesiredConfidence()
                .map(FeeRecommendationRequest.Confidence::getConfidenceValue)
                .map(requestedConf -> confidenceMap.keySet().stream()
                        .mapToDouble(val -> val)
                        .filter(supportedConf -> supportedConf >= requestedConf)
                        .min())
                .map(val -> {
                    // ATTENTION: this may not always be what the user wants!
                    // if the key is not present, the user must have specified a value higher
                    // than the highest confidence value supported by this api.
                    // in this case, take the highest value and continue.
                    // e.g. user specified 95% confidence, he will get 90%.
                    // let the user decide whether to take the recommendation or use other results.
                    return val.orElse(highestConfidenceKey);
                })
                .map(confidenceMap::get);
    }
}

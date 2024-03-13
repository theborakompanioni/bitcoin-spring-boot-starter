package org.tbk.bitcoin.tool.fee.strike;

import lombok.extern.slf4j.Slf4j;
import org.tbk.bitcoin.tool.fee.*;
import org.tbk.bitcoin.tool.fee.FeeRecommendationResponseImpl.FeeRecommendationImpl;
import org.tbk.bitcoin.tool.fee.FeeRecommendationResponseImpl.SatPerVbyteImpl;
import org.tbk.bitcoin.tool.fee.strike.proto.BlendedFeeEstimateResponse;
import org.tbk.bitcoin.tool.fee.util.MoreBitcoin;
import org.tbk.bitcoin.tool.fee.util.MoreSatPerVbyte;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Map;
import java.util.OptionalLong;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Slf4j
public class StrikeFeeProvider extends AbstractFeeProvider {

    private static final ProviderInfo providerInfo = ProviderInfo.SimpleProviderInfo.builder()
            .name("Strike")
            .description("")
            .build();

    private static final int MAX_BLOCK_TARGET = 576;

    private final StrikeFeeApiClient client;

    public StrikeFeeProvider(StrikeFeeApiClient client) {
        super(providerInfo);

        this.client = requireNonNull(client);
    }

    @Override
    public boolean supports(FeeRecommendationRequest request) {
        return request.getDesiredConfidence().isEmpty() &&
               request.getBlockTarget() <= MAX_BLOCK_TARGET;
    }

    @Override
    public Flux<FeeRecommendationResponse> requestHook(FeeRecommendationRequest feeRecommendationRequest) {
        return Mono.fromCallable(() -> requestHookInternal(feeRecommendationRequest)).flux();
    }

    @Nullable
    private FeeRecommendationResponse requestHookInternal(FeeRecommendationRequest feeRecommendationRequest) {
        BlendedFeeEstimateResponse response = client.feeEstimates();

        Map<String, Long> estimateMap = response.getFeeByBlockTargetMap();
        if (estimateMap.isEmpty()) {
            log.warn("No estimation entries present in response for request.");
            return null;
        }

        long minutes = feeRecommendationRequest.getDurationTarget().toMinutes();

        Map<Long, Long> estimateMapWithMinutesAsKeys = estimateMap.entrySet().stream()
                .collect(Collectors.toMap(val -> MoreBitcoin.averageBlockDuration(Long.valueOf(val.getKey(), 10)).toMinutes(), Map.Entry::getValue));

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
        Long estimate = estimateMapWithMinutesAsKeys.get(minutesKey);

        return FeeRecommendationResponseImpl.builder()
                .addFeeRecommendation(FeeRecommendationImpl.builder()
                        .feeUnit(SatPerVbyteImpl.builder()
                                .satPerVbyteValue(MoreSatPerVbyte.fromSatPerKVbyte(BigDecimal.valueOf(estimate)))
                                .build())
                        .build())
                .build();
    }
}

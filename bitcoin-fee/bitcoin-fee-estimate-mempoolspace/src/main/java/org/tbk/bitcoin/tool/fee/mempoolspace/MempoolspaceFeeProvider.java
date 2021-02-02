package org.tbk.bitcoin.tool.fee.mempoolspace;

import lombok.extern.slf4j.Slf4j;
import org.tbk.bitcoin.tool.fee.*;
import org.tbk.bitcoin.tool.fee.FeeRecommendationResponseImpl.SatPerVbyteImpl.SatPerVbyteImplBuilder;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.time.Duration;

import static java.util.Objects.requireNonNull;

@Slf4j
public class MempoolspaceFeeProvider extends AbstractFeeProvider {
    private static final ProviderInfo providerInfo = ProviderInfo.SimpleProviderInfo.builder()
            .name("mempool.space")
            .description("")
            .build();

    private static final Duration HALF_HOUR = Duration.ofMinutes(30);
    private static final Duration HOUR = Duration.ofHours(1);

    private final MempoolspaceFeeApiClient client;

    public MempoolspaceFeeProvider(MempoolspaceFeeApiClient client) {
        super(providerInfo);

        this.client = requireNonNull(client);
    }

    @Override
    public boolean supports(FeeRecommendationRequest request) {
        return request.getDesiredConfidence().isEmpty() &&
                request.getDurationTarget().compareTo(HOUR) <= 0;
    }

    @Override
    protected Flux<FeeRecommendationResponse> requestHook(FeeRecommendationRequest request) {
        FeesRecommended feesRecommended = this.client.feesRecommended();

        SatPerVbyteImplBuilder feeBuilder = FeeRecommendationResponseImpl.SatPerVbyteImpl.builder();
        if (request.isNextBlockTarget()) {
            feeBuilder.satPerVbyteValue(BigDecimal.valueOf(feesRecommended.getFastestFee()));
        } else if (request.getDurationTarget().compareTo(HALF_HOUR) <= 0) {
            feeBuilder.satPerVbyteValue(BigDecimal.valueOf(feesRecommended.getHalfHourFee()));
        } else if (request.getDurationTarget().compareTo(HOUR) <= 0) {
            feeBuilder.satPerVbyteValue(BigDecimal.valueOf(feesRecommended.getHourFee()));
        } else {
            throw new IllegalStateException("Unsupported request: duration target is greater than " + HOUR);
        }

        return Flux.just(FeeRecommendationResponseImpl.builder()
                .addFeeRecommendation(FeeRecommendationResponseImpl.FeeRecommendationImpl.builder()
                        .feeUnit(feeBuilder.build())
                        .build())
                .build());
    }
}

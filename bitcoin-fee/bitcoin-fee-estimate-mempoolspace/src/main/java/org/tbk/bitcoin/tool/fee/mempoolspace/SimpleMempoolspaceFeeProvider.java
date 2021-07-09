package org.tbk.bitcoin.tool.fee.mempoolspace;

import lombok.extern.slf4j.Slf4j;
import org.tbk.bitcoin.tool.fee.*;
import org.tbk.bitcoin.tool.fee.FeeRecommendationResponseImpl.FeeRecommendationImpl;
import org.tbk.bitcoin.tool.fee.FeeRecommendationResponseImpl.SatPerVbyteImpl;
import org.tbk.bitcoin.tool.fee.FeeRecommendationResponseImpl.SatPerVbyteImpl.SatPerVbyteImplBuilder;
import org.tbk.bitcoin.tool.fee.ProviderInfo.SimpleProviderInfo;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.time.Duration;

import static java.util.Objects.requireNonNull;

@Slf4j
public class SimpleMempoolspaceFeeProvider extends AbstractFeeProvider {

    private static final SimpleProviderInfo defaultProviderInfo = SimpleProviderInfo.builder()
            .name("mempool.space-simple")
            .description("Simple fee recommendation")
            .build();

    private static final Duration HALF_HOUR = Duration.ofMinutes(30);
    private static final Duration HOUR = Duration.ofHours(1);

    private final MempoolspaceFeeApiClient client;

    public SimpleMempoolspaceFeeProvider(MempoolspaceFeeApiClient client) {
        this(client, defaultProviderInfo);
    }

    public SimpleMempoolspaceFeeProvider(MempoolspaceFeeApiClient client, ProviderInfo providerInfo) {
        super(providerInfo);

        this.client = requireNonNull(client);
    }

    @Override
    public boolean supports(FeeRecommendationRequest request) {
        return request.getDesiredConfidence().isEmpty()
                && request.getDurationTarget().compareTo(HOUR) <= 0;
    }

    @Override
    protected Flux<FeeRecommendationResponse> requestHook(FeeRecommendationRequest request) {
        FeesRecommended feesRecommended = this.client.feesRecommended();

        SatPerVbyteImplBuilder feeBuilder = SatPerVbyteImpl.builder();
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
                .addFeeRecommendation(FeeRecommendationImpl.builder()
                        .feeUnit(feeBuilder.build())
                        .build())
                .build());
    }
}

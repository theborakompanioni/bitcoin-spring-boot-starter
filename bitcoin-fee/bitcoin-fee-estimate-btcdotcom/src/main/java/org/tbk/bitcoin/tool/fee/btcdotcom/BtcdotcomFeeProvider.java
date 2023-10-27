package org.tbk.bitcoin.tool.fee.btcdotcom;

import lombok.extern.slf4j.Slf4j;
import org.tbk.bitcoin.tool.fee.*;
import org.tbk.bitcoin.tool.fee.btcdotcom.proto.FeeDistribution;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;

import static java.util.Objects.requireNonNull;

@Slf4j
public class BtcdotcomFeeProvider extends AbstractFeeProvider {

    private static final ProviderInfo providerInfo = ProviderInfo.SimpleProviderInfo.builder()
            .name("btc.com")
            .description("")
            .build();

    private final BtcdotcomFeeApiClient client;

    public BtcdotcomFeeProvider(BtcdotcomFeeApiClient client) {
        super(providerInfo);

        this.client = requireNonNull(client);
    }

    @Override
    public boolean supports(FeeRecommendationRequest request) {
        return request.getDesiredConfidence().isEmpty()
                && !request.isTargetDurationZeroOrLess()
                && request.isNextBlockTarget();
    }

    @Override
    protected Flux<FeeRecommendationResponse> requestHook(FeeRecommendationRequest request) {
        FeeDistribution feeDistribution = this.client.feeDistribution();

        BigDecimal satPerVbyteValue = BigDecimal.valueOf(feeDistribution.getFeesRecommended().getOneBlockFee());
        FeeRecommendationResponseImpl.SatPerVbyteImpl satPerVbyte = FeeRecommendationResponseImpl.SatPerVbyteImpl.builder()
                .satPerVbyteValue(satPerVbyteValue)
                .build();

        return Flux.just(FeeRecommendationResponseImpl.builder()
                .addFeeRecommendation(FeeRecommendationResponseImpl.FeeRecommendationImpl.builder()
                        .feeUnit(satPerVbyte)
                        .build())
                .build());
    }
}

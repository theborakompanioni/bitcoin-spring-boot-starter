package org.tbk.bitcoin.tool.fee.bitcore;

import org.tbk.bitcoin.tool.fee.*;
import org.tbk.bitcoin.tool.fee.FeeRecommendationResponseImpl.SatPerVbyteImpl;
import org.tbk.bitcoin.tool.fee.util.MoreSatPerVbyte;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;

import static java.util.Objects.requireNonNull;

public class BitcoreFeeProvider extends AbstractFeeProvider {

    private static final ProviderInfo providerInfo = ProviderInfo.SimpleProviderInfo.builder()
            .name("Bitcore")
            .description("")
            .build();

    private final BitcoreFeeApiClient client;

    public BitcoreFeeProvider(BitcoreFeeApiClient client) {
        super(providerInfo);

        this.client = requireNonNull(client);
    }

    @Override
    public boolean supports(FeeRecommendationRequest request) {
        return request.getDesiredConfidence().isEmpty();
    }

    @Override
    protected Flux<FeeRecommendationResponse> requestHook(FeeRecommendationRequest request) {
        FeeEstimationResponse feeEstimationResponse = client.bitcoinMainnetFee(buildApiRequest(request));

        double btcPerKB = feeEstimationResponse.getFeerate();

        SatPerVbyteImpl satPerVbyte = SatPerVbyteImpl.builder()
                .satPerVbyteValue(MoreSatPerVbyte.fromBtcPerKVbyte(BigDecimal.valueOf(btcPerKB)))
                .build();

        return Flux.just(FeeRecommendationResponseImpl.builder()
                .addFeeRecommendation(FeeRecommendationResponseImpl.FeeRecommendationImpl.builder()
                        .satPerVbyte(satPerVbyte)
                        .build())
                .build());
    }

    private FeeEstimationRequest buildApiRequest(FeeRecommendationRequest request) {
        return FeeEstimationRequest.newBuilder()
                .setBlocks(request.getBlockTarget())
                .build();
    }
}

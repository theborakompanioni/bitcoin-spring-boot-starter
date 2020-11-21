package org.tbk.bitcoin.tool.fee.bitgo;

import lombok.extern.slf4j.Slf4j;
import org.tbk.bitcoin.tool.fee.*;
import org.tbk.bitcoin.tool.fee.FeeRecommendationResponseImpl.FeeRecommendationImpl;
import org.tbk.bitcoin.tool.fee.FeeRecommendationResponseImpl.SatPerVbyteImpl;
import org.tbk.bitcoin.tool.fee.ProviderInfo.SimpleProviderInfo;
import org.tbk.bitcoin.tool.fee.util.MoreSatPerVbyte;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;

import static java.util.Objects.requireNonNull;

@Slf4j
public class BitgoFeeProvider extends AbstractFeeProvider {
    private static final ProviderInfo providerInfo = SimpleProviderInfo.builder()
            .name("Bitgo")
            .description("")
            .build();

    private final BitgoFeeApiClient client;

    public BitgoFeeProvider(BitgoFeeApiClient client) {
        super(providerInfo);

        this.client = requireNonNull(client);
    }

    @Override
    public boolean supports(FeeRecommendationRequest request) {
        // while bitgo returns a "confidence" property, it does not support it as request param
        return request.getDesiredConfidence().isEmpty();
    }

    @Override
    protected Flux<FeeRecommendationResponse> requestHook(FeeRecommendationRequest request) {
        BtcTxFeeRequest apiRequest = buildApiRequest(request);
        BtcTxFeeResponse response = client.btcTxFee(apiRequest);

        log.debug("data: {}", response);

        long satPerKilobyte = response.getFeePerKb();

        SatPerVbyteImpl satPerVbyte = SatPerVbyteImpl.builder()
                .satPerVbyteValue(MoreSatPerVbyte.fromSatPerKVbyte(BigDecimal.valueOf(satPerKilobyte)))
                .build();

        return Flux.just(FeeRecommendationResponseImpl.builder()
                .addFeeRecommendation(FeeRecommendationImpl.builder()
                        .feeUnit(satPerVbyte)
                        .build())
                .build());
    }

    private BtcTxFeeRequest buildApiRequest(FeeRecommendationRequest request) {
        return BtcTxFeeRequest.newBuilder()
                .setNumBlocks(request.getBlockTarget())
                .build();
    }
}

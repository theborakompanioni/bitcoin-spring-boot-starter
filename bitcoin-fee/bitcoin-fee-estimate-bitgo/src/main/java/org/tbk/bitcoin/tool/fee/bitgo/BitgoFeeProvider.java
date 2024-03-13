package org.tbk.bitcoin.tool.fee.bitgo;

import org.tbk.bitcoin.tool.fee.*;
import org.tbk.bitcoin.tool.fee.FeeRecommendationResponseImpl.FeeRecommendationImpl;
import org.tbk.bitcoin.tool.fee.FeeRecommendationResponseImpl.SatPerVbyteImpl;
import org.tbk.bitcoin.tool.fee.ProviderInfo.SimpleProviderInfo;
import org.tbk.bitcoin.tool.fee.bitgo.proto.BtcTxFeeRequest;
import org.tbk.bitcoin.tool.fee.bitgo.proto.BtcTxFeeResponse;
import org.tbk.bitcoin.tool.fee.util.MoreSatPerVbyte;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static java.util.Objects.requireNonNull;

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
        boolean isConfidenceEmpty = request.getDesiredConfidence().isEmpty();

        // "bitgo" throws errors if block target is below 2.
        // the "feeByBlockTarget" in the response is very unreliable:
        // it contains other values than when requests with given "block target" param - strange.
        boolean isBlockTargetSupported = request.getBlockTarget() >= 2;

        return isConfidenceEmpty && isBlockTargetSupported;
    }

    @Override
    protected Flux<FeeRecommendationResponse> requestHook(FeeRecommendationRequest request) {
        return Mono.fromCallable(() -> requestHookInternal(request)).flux();
    }

    private FeeRecommendationResponse requestHookInternal(FeeRecommendationRequest request) {
        BtcTxFeeRequest apiRequest = buildApiRequest(request);
        BtcTxFeeResponse response = client.btcTxFee(apiRequest);

        long satPerKilobyte = response.getFeePerKb();

        SatPerVbyteImpl satPerVbyte = SatPerVbyteImpl.builder()
                .satPerVbyteValue(MoreSatPerVbyte.fromSatPerKVbyte(BigDecimal.valueOf(satPerKilobyte)))
                .build();

        return FeeRecommendationResponseImpl.builder()
                .addFeeRecommendation(FeeRecommendationImpl.builder()
                        .feeUnit(satPerVbyte)
                        .build())
                .build();
    }

    private BtcTxFeeRequest buildApiRequest(FeeRecommendationRequest request) {
        return BtcTxFeeRequest.newBuilder()
                .setNumBlocks(request.getBlockTarget())
                .build();
    }
}

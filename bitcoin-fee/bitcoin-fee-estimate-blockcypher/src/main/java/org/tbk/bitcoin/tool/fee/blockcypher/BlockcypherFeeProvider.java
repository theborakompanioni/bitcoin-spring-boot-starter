package org.tbk.bitcoin.tool.fee.blockcypher;

import lombok.extern.slf4j.Slf4j;
import org.tbk.bitcoin.tool.fee.*;
import org.tbk.bitcoin.tool.fee.FeeRecommendationResponseImpl.FeeRecommendationImpl;
import org.tbk.bitcoin.tool.fee.FeeRecommendationResponseImpl.SatPerVbyteImpl;
import org.tbk.bitcoin.tool.fee.blockcypher.proto.ChainInfo;
import org.tbk.bitcoin.tool.fee.util.MoreBitcoin;
import org.tbk.bitcoin.tool.fee.util.MoreSatPerVbyte;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;

import static java.util.Objects.requireNonNull;

@Slf4j
public class BlockcypherFeeProvider extends AbstractFeeProvider {

    private static final Duration HIGH_FEE_DURATION_TARGET = MoreBitcoin.averageBlockDuration(2);
    private static final Duration MEDIUM_FEE_DURATION_TARGET = MoreBitcoin.averageBlockDuration(7);
    private static final Duration LOW_FEE_DURATION_TARGET = MoreBitcoin.averageBlockDuration(9);

    private static final ProviderInfo providerInfo = ProviderInfo.SimpleProviderInfo.builder()
            .name("Blockcypher")
            .description("")
            .build();

    private final BlockcypherFeeApiClient client;

    public BlockcypherFeeProvider(BlockcypherFeeApiClient client) {
        super(providerInfo);

        this.client = requireNonNull(client);
    }

    @Override
    public boolean supports(FeeRecommendationRequest request) {
        return request.getDesiredConfidence().isEmpty()
               && LOW_FEE_DURATION_TARGET.compareTo(request.getDurationTarget()) >= 0;
    }

    @Override
    protected Flux<FeeRecommendationResponse> requestHook(FeeRecommendationRequest request) {
        return Mono.fromCallable(() -> requestHookInternal(request)).flux();
    }

    private FeeRecommendationResponse requestHookInternal(FeeRecommendationRequest request) {
        ChainInfo chainInfo = this.client.btcMain();

        BigDecimal satPerKByte = BigDecimal.valueOf(getSatBerKByte(request, chainInfo));
        BigDecimal satPerVbyte = MoreSatPerVbyte.fromSatPerKVbyte(satPerKByte);

        return FeeRecommendationResponseImpl.builder()
                .addFeeRecommendation(FeeRecommendationImpl.builder()
                        .feeUnit(SatPerVbyteImpl.builder()
                                .satPerVbyteValue(satPerVbyte)
                                .build())
                        .build())
                .build();
    }

    private long getSatBerKByte(FeeRecommendationRequest request, ChainInfo chainInfo) {
        if (request.isNextBlockTarget()) {
            return chainInfo.getHighFeePerKb();
        }

        boolean isWithinHighFeeTarget = HIGH_FEE_DURATION_TARGET.compareTo(request.getDurationTarget()) >= 0;
        if (isWithinHighFeeTarget) {
            return (chainInfo.getHighFeePerKb() + chainInfo.getMediumFeePerKb()) / 2;
        }

        boolean isWithinMediumFeeTarget = MEDIUM_FEE_DURATION_TARGET.compareTo(request.getDurationTarget()) >= 0;
        if (isWithinMediumFeeTarget) {
            return chainInfo.getMediumFeePerKb();
        }

        boolean isWithinLowFeeTarget = LOW_FEE_DURATION_TARGET.compareTo(request.getDurationTarget()) >= 0;
        if (isWithinLowFeeTarget) {
            return chainInfo.getLowFeePerKb();
        }

        return 1L;
    }
}

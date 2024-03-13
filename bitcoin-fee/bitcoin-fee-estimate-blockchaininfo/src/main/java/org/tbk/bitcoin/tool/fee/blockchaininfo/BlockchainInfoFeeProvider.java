package org.tbk.bitcoin.tool.fee.blockchaininfo;

import lombok.extern.slf4j.Slf4j;
import org.tbk.bitcoin.tool.fee.*;
import org.tbk.bitcoin.tool.fee.FeeRecommendationResponseImpl.SatPerVbyteImpl;
import org.tbk.bitcoin.tool.fee.blockchaininfo.proto.MempoolFees;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;

import static java.util.Objects.requireNonNull;

@Slf4j
public class BlockchainInfoFeeProvider extends AbstractFeeProvider {
    private static final Duration MAX_DURATION_TARGET = Duration.ofMinutes(60 * 6);

    private static final ProviderInfo providerInfo = ProviderInfo.SimpleProviderInfo.builder()
            .name("Blockchain.info")
            .description("")
            .build();

    private final BlockchainInfoFeeApiClient client;

    public BlockchainInfoFeeProvider(BlockchainInfoFeeApiClient client) {
        super(providerInfo);

        this.client = requireNonNull(client);
    }

    @Override
    public boolean supports(FeeRecommendationRequest request) {
        // blockchain.info fees do not support any customized request
        return request.getDesiredConfidence().isEmpty()
               && Duration.ofMinutes(360).compareTo(request.getDurationTarget()) >= 0;
    }

    @Override
    protected Flux<FeeRecommendationResponse> requestHook(FeeRecommendationRequest request) {
        return Mono.fromCallable(() -> requestHookInternal(request)).flux();
    }

    private FeeRecommendationResponse requestHookInternal(FeeRecommendationRequest request) {
        MempoolFees mempoolFees = client.mempoolFees();

        boolean isLessOrEqualToSixHours = MAX_DURATION_TARGET.compareTo(request.getDurationTarget()) >= 0;

        if (!isLessOrEqualToSixHours) {
            log.warn("Unsupported call to Blockchain.info fee provider: period out of range: {} > {}",
                    request.getDurationTarget(), MAX_DURATION_TARGET);
            return null;
        }

        boolean isZeroOrLess = Duration.ZERO.compareTo(request.getDurationTarget()) >= 0;
        boolean isLessOrEqualToHalfHour = Duration.ofMinutes(30).compareTo(request.getDurationTarget()) >= 0;

        long satsPerByte = isZeroOrLess ?
                calcSatPerVbyteForNextBlock(mempoolFees) :
                isLessOrEqualToHalfHour ?
                        mempoolFees.getPriority() :
                        mempoolFees.getRegular();

        SatPerVbyteImpl satPerVbyte = SatPerVbyteImpl.builder()
                .satPerVbyteValue(BigDecimal.valueOf(satsPerByte))
                .build();

        return FeeRecommendationResponseImpl.builder()
                .addFeeRecommendation(FeeRecommendationResponseImpl.FeeRecommendationImpl.builder()
                        .feeUnit(satPerVbyte)
                        .build())
                .build();
    }

    /**
     * NEVER EVER RETURN the maximum of the mempool - this way we would be vulnerable to overpaying extremely
     * This just goes to show that APIs such as blockchain.info are not
     * very well suited for this a "block target" approach..
     * TODO: consider removing the blockchain.info module entirely
     */
    private long calcSatPerVbyteForNextBlock(MempoolFees mempoolFees) {
        return (mempoolFees.getPriority() + mempoolFees.getLimit().getMax()) / 2;
    }
}

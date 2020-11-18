package org.tbk.bitcoin.tool.fee.blockchaininfo;

import lombok.extern.slf4j.Slf4j;
import org.tbk.bitcoin.tool.fee.*;
import org.tbk.bitcoin.tool.fee.FeeRecommendationResponseImpl.SatPerVbyteImpl;
import reactor.core.publisher.Flux;

import java.time.Duration;

import static java.util.Objects.requireNonNull;

@Slf4j
public class BlockchainInfoFeeProvider extends AbstractFeeProvider {
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
        return request.getDesiredConfidence().isEmpty() &&
                Duration.ofMinutes(360).compareTo(request.getDurationTarget()) >= 0;
    }

    @Override
    public Flux<FeeRecommendationResponse> requestHook(FeeRecommendationRequest request) {
        MempoolFees mempoolFees = client.mempoolFees();

        log.debug("data: {}", mempoolFees);

        boolean isLessOrEqualToSixHours = Duration.ofMinutes(60 * 6).compareTo(request.getDurationTarget()) >= 0;

        if (!isLessOrEqualToSixHours) {
            log.warn("Unsupported call to Blockchain.info fee provider: period out of range: {}", request);
            return Flux.empty();
        }

        boolean isZeroOrLess = Duration.ZERO.compareTo(request.getDurationTarget()) >= 0;
        boolean isLessOrEqualToHalfHour = Duration.ofMinutes(30).compareTo(request.getDurationTarget()) >= 0;

        long satsPerByte = isZeroOrLess ?
                mempoolFees.getLimit().getMax() :
                isLessOrEqualToHalfHour ?
                        mempoolFees.getPriority() :
                        mempoolFees.getRegular();

        SatPerVbyteImpl satPerVbyte = SatPerVbyteImpl.fromSatPerByte(satsPerByte);

        return Flux.just(FeeRecommendationResponseImpl.builder()
                .addFeeRecommendation(FeeRecommendationResponseImpl.FeeRecommendationImpl.builder()
                        .satPerVbyte(satPerVbyte)
                        .build())
                .build());
    }
}

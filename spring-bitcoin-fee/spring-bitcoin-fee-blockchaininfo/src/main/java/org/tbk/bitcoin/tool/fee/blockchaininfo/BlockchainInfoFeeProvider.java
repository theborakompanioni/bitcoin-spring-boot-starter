package org.tbk.bitcoin.tool.fee.blockchaininfo;

import lombok.extern.slf4j.Slf4j;
import org.tbk.bitcoin.tool.fee.*;
import org.tbk.bitcoin.tool.fee.FeeRecommendationResponseImpl.SatPerVbyteImpl;
import reactor.core.publisher.Flux;

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
                request.getDurationTarget().isEmpty();
    }

    @Override
    public Flux<FeeRecommendationResponse> requestHook(FeeRecommendationRequest request) {
        MempoolFees mempoolFees = client.mempoolFees();

        log.debug("data: {}", mempoolFees);

        long satsPerByte = mempoolFees.getPriority();

        SatPerVbyteImpl satPerVbyte = SatPerVbyteImpl.fromSatPerByte(satsPerByte);

        return Flux.just(FeeRecommendationResponseImpl.builder()
                .addFeeRecommendation(FeeRecommendationResponseImpl.FeeRecommendationImpl.builder()
                        .satPerVbyte(satPerVbyte)
                        .build())
                .build());
    }
}

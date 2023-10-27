package org.tbk.bitcoin.tool.fee.blockstreaminfo;

import lombok.extern.slf4j.Slf4j;
import org.tbk.bitcoin.tool.fee.*;
import org.tbk.bitcoin.tool.fee.FeeRecommendationResponseImpl.SatPerVbyteImpl;
import org.tbk.bitcoin.tool.fee.blockstreaminfo.proto.FeeEstimates;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Slf4j
public class BlockstreamInfoFeeProvider extends AbstractFeeProvider {

    private static final ProviderInfo providerInfo = ProviderInfo.SimpleProviderInfo.builder()
            .name("blockstream.info")
            .description("")
            .build();

    private final BlockstreamInfoFeeApiClient client;

    public BlockstreamInfoFeeProvider(BlockstreamInfoFeeApiClient client) {
        super(providerInfo);

        this.client = requireNonNull(client);
    }

    @Override
    public boolean supports(FeeRecommendationRequest request) {
        return request.getDesiredConfidence().isEmpty();
    }

    protected Flux<FeeRecommendationResponse> requestHook(FeeRecommendationRequest request) {
        FeeEstimates feeEstimates = client.feeEstimates();

        Optional<FeeEstimates.Entry> feeEstimateOrEmpty = feeEstimates.getEntryList().stream()
                .filter(val -> val.getNumberOfBlocks() <= request.getBlockTarget())
                .max(Comparator.comparingLong(FeeEstimates.Entry::getNumberOfBlocks));

        if (feeEstimateOrEmpty.isEmpty()) {
            log.warn("No suitable estimation entries present in response for request.");
            return Flux.empty();
        }

        BigDecimal satPerVbyteValue = BigDecimal.valueOf(feeEstimateOrEmpty.get().getEstimatedFeerateInSatPerVbyte());

        SatPerVbyteImpl satPerVbyte = SatPerVbyteImpl.builder()
                .satPerVbyteValue(satPerVbyteValue)
                .build();

        return Flux.just(FeeRecommendationResponseImpl.builder()
                .addFeeRecommendation(FeeRecommendationResponseImpl.FeeRecommendationImpl.builder()
                        .feeUnit(satPerVbyte)
                        .build())
                .build());
    }
}

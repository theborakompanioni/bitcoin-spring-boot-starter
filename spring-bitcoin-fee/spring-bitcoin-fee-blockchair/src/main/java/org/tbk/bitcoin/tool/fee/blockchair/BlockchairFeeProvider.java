package org.tbk.bitcoin.tool.fee.blockchair;

import lombok.extern.slf4j.Slf4j;
import org.tbk.bitcoin.tool.fee.*;
import org.tbk.bitcoin.tool.fee.util.MoreBitcoin;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.time.Duration;

import static java.util.Objects.requireNonNull;

@Slf4j
public class BlockchairFeeProvider extends AbstractFeeProvider {
    // blockchair only delivery one value "suggested_transaction_fee_per_byte_sat".
    // compared with whatthefee.io this seems to be a suggestion for the next 3 blocks
    // (blockchair itself says, this if for the very next block => seems to be broader than that)
    private static final Duration MAX_DURATION_TARGET = MoreBitcoin.averageBlockDuration(3);

    private static final ProviderInfo providerInfo = ProviderInfo.SimpleProviderInfo.builder()
            .name("Blockchair.com")
            .description("")
            .build();

    private final BlockchairFeeApiClient client;

    public BlockchairFeeProvider(BlockchairFeeApiClient client) {
        super(providerInfo);

        this.client = requireNonNull(client);
    }

    @Override
    public boolean supports(FeeRecommendationRequest request) {
        return request.getDesiredConfidence().isEmpty() &&
                !request.isTargetDurationZeroOrLess() &&
                MAX_DURATION_TARGET.compareTo(request.getDurationTarget()) >= 0;
    }

    @Override
    protected Flux<FeeRecommendationResponse> requestHook(FeeRecommendationRequest request) {
        BitcoinStatsFeesOnly bitcoinStatsFeesOnly = this.client.bitcoinStatsFeesOnly();

        FeeRecommendationResponseImpl.SatPerVbyteImpl satPerVbyte = FeeRecommendationResponseImpl.SatPerVbyteImpl.builder()
                .satPerVbyteValue(BigDecimal.valueOf(bitcoinStatsFeesOnly.getData().getSuggestedTransactionFeePerByteSat()))
                .build();

        return Flux.just(FeeRecommendationResponseImpl.builder()
                .addFeeRecommendation(FeeRecommendationResponseImpl.FeeRecommendationImpl.builder()
                        .feeUnit(satPerVbyte)
                        .build())
                .build());
    }
}

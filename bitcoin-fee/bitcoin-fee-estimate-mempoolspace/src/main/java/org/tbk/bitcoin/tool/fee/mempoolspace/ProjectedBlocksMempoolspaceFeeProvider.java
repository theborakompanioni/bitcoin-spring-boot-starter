package org.tbk.bitcoin.tool.fee.mempoolspace;

import com.google.common.base.Suppliers;
import lombok.extern.slf4j.Slf4j;
import org.tbk.bitcoin.tool.fee.*;
import org.tbk.bitcoin.tool.fee.FeeRecommendationResponseImpl.FeeRecommendationImpl;
import org.tbk.bitcoin.tool.fee.FeeRecommendationResponseImpl.SatPerVbyteImpl;
import org.tbk.bitcoin.tool.fee.FeeRecommendationResponseImpl.SatPerVbyteImpl.SatPerVbyteImplBuilder;
import org.tbk.bitcoin.tool.fee.mempoolspace.ProjectedMempoolBlocks.ProjectedBlock;
import org.tbk.bitcoin.tool.fee.util.MoreBitcoin;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

@Slf4j
public class ProjectedBlocksMempoolspaceFeeProvider extends AbstractFeeProvider {
    private static final ProviderInfo providerInfo = ProviderInfo.SimpleProviderInfo.builder()
            .name("mempool.space")
            .description("Fee recommendation using projected blocks of the mempool")
            .build();

    private static final long MAX_PROJECTED_BLOCKS_IN_RESPONSE = 8;
    private static final Duration MAX_DURATION = MoreBitcoin.averageBlockDuration(MAX_PROJECTED_BLOCKS_IN_RESPONSE);


    private final Supplier<ProjectedMempoolBlocks> projectedMempoolBlocksSupplier;

    public ProjectedBlocksMempoolspaceFeeProvider(MempoolspaceFeeApiClient client) {
        this(client, Duration.ofSeconds(3));
    }

    public ProjectedBlocksMempoolspaceFeeProvider(MempoolspaceFeeApiClient client, Duration cacheTimeout) {
        super(providerInfo);
        requireNonNull(client);
        requireNonNull(cacheTimeout);
        checkArgument(!cacheTimeout.isNegative(), "'cacheTimeout' must not be negative");

        this.projectedMempoolBlocksSupplier = Suppliers.memoizeWithExpiration(client::projectedBlocks, cacheTimeout.toSeconds(), TimeUnit.SECONDS);
    }

    @Override
    public boolean supports(FeeRecommendationRequest request) {
        return request.getDesiredConfidence().isEmpty() &&
                request.getDurationTarget().compareTo(MAX_DURATION) <= 0;
    }

    @Override
    protected Flux<FeeRecommendationResponse> requestHook(FeeRecommendationRequest request) {
        ProjectedMempoolBlocks projectedBlocks = this.projectedMempoolBlocksSupplier.get();

        boolean isInRange = request.getBlockTarget() <= projectedBlocks.getBlocksCount();
        if (!isInRange) {
            return Flux.empty();
        }

        if (request.isTargetDurationZeroOrLess()) {
            ProjectedBlock firstBlock = projectedBlocks.getBlocks(0);

            double averageFee = firstBlock.getBlockVsize() / (double) firstBlock.getTotalFees();

            double highestFeeInBlock = Optional.of(firstBlock)
                    .filter(val -> val.getFeeRangeCount() > 0)
                    .map(val -> val.getFeeRange(val.getFeeRangeCount() - 1))
                    .orElse(averageFee * 1.2d);

            double medianFee = firstBlock.getMedianFee();
            double medianDifferenceToHighestFee = Math.max(0, highestFeeInBlock - medianFee);

            double value = Math.min(medianFee * 1.33d, medianFee + (medianDifferenceToHighestFee / 2));

            return Flux.just(FeeRecommendationResponseImpl.builder()
                    .addFeeRecommendation(FeeRecommendationImpl.builder()
                            .feeUnit(SatPerVbyteImpl.builder()
                                    .satPerVbyteValue(BigDecimal.valueOf(value))
                                    .build())
                            .build())
                    .build());
        }

        int index = Math.max(0, ((int) request.getBlockTarget()) - 1);
        ProjectedBlock block = projectedBlocks.getBlocks(index);

        SatPerVbyteImplBuilder feeBuilder = SatPerVbyteImpl.builder();
        feeBuilder.satPerVbyteValue(BigDecimal.valueOf(block.getMedianFee()));

        return Flux.just(FeeRecommendationResponseImpl.builder()
                .addFeeRecommendation(FeeRecommendationImpl.builder()
                        .feeUnit(feeBuilder.build())
                        .build())
                .build());
    }
}

package org.tbk.bitcoin.tool.fee.mempoolspace;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Suppliers;
import lombok.extern.slf4j.Slf4j;
import org.tbk.bitcoin.tool.fee.*;
import org.tbk.bitcoin.tool.fee.FeeRecommendationResponse.FeeUnit;
import org.tbk.bitcoin.tool.fee.FeeRecommendationResponseImpl.FeeRecommendationImpl;
import org.tbk.bitcoin.tool.fee.FeeRecommendationResponseImpl.SatPerVbyteImpl;
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

    public interface FeesFromProjectedBlockStrategy {
        FeeUnit get(FeeRecommendationRequest request, ProjectedBlock projectedBlock);
    }

    private static final ProviderInfo providerInfo = ProviderInfo.SimpleProviderInfo.builder()
            .name("mempool.space")
            .description("Fee recommendation using projected blocks of the mempool")
            .build();

    private static final long MAX_PROJECTED_BLOCKS_IN_RESPONSE = 8;
    private static final Duration MAX_DURATION = MoreBitcoin.averageBlockDuration(MAX_PROJECTED_BLOCKS_IN_RESPONSE);

    private static final Duration DEFAULT_CACHE_TIMEOUT = Duration.ofSeconds(3);
    private static final FeesFromProjectedBlockStrategy DEFAULT_STRATEGY = new DefaultFeesFromProjectedBlockStrategy();

    private final Supplier<ProjectedMempoolBlocks> projectedMempoolBlocksSupplier;
    private final FeesFromProjectedBlockStrategy feesFromProjectedBlockSupplier;

    public ProjectedBlocksMempoolspaceFeeProvider(MempoolspaceFeeApiClient client) {
        this(client, DEFAULT_CACHE_TIMEOUT);
    }

    @VisibleForTesting
    ProjectedBlocksMempoolspaceFeeProvider(MempoolspaceFeeApiClient client, Duration cacheDuration) {
        this(client, DEFAULT_STRATEGY, cacheDuration);
    }

    public ProjectedBlocksMempoolspaceFeeProvider(MempoolspaceFeeApiClient client,
                                                  FeesFromProjectedBlockStrategy feesFromProjectedBlockSupplier) {
        this(client, feesFromProjectedBlockSupplier, DEFAULT_CACHE_TIMEOUT);
    }

    public ProjectedBlocksMempoolspaceFeeProvider(MempoolspaceFeeApiClient client,
                                                  FeesFromProjectedBlockStrategy feesFromProjectedBlockSupplier,
                                                  Duration cacheDuration) {
        super(providerInfo);

        requireNonNull(client);
        requireNonNull(cacheDuration);
        checkArgument(!cacheDuration.isNegative(), "'cacheDuration' must not be negative");

        this.feesFromProjectedBlockSupplier = requireNonNull(feesFromProjectedBlockSupplier);
        this.projectedMempoolBlocksSupplier = Suppliers.memoizeWithExpiration(client::projectedBlocks, cacheDuration.toSeconds(), TimeUnit.SECONDS);
    }

    @Override
    public boolean supports(FeeRecommendationRequest request) {
        return request.getDesiredConfidence().isEmpty() &&
                request.getDurationTarget().compareTo(MAX_DURATION) <= 0;
    }

    @Override
    protected Flux<FeeRecommendationResponse> requestHook(FeeRecommendationRequest request) {
        ProjectedMempoolBlocks projectedBlocks = this.projectedMempoolBlocksSupplier.get();

        boolean isBlockInRange = request.getBlockTarget() <= projectedBlocks.getBlocksCount();
        if (!isBlockInRange) {
            return Flux.empty();
        }

        int index = Math.max(0, ((int) request.getBlockTarget()) - 1);
        ProjectedBlock projectedBlock = projectedBlocks.getBlocks(index);

        FeeUnit feeRate = feesFromProjectedBlockSupplier.get(request, projectedBlock);

        return Flux.just(FeeRecommendationResponseImpl.builder()
                .addFeeRecommendation(FeeRecommendationImpl.builder()
                        .feeUnit(feeRate)
                        .build())
                .build());
    }

    private static final class DefaultFeesFromProjectedBlockStrategy implements FeesFromProjectedBlockStrategy {
        private static final double TWENTY_PERCENT_MULTIPLIER = 1.2d;
        private static final double THIRTYTHREE_PERCENT_MULTIPLIER = 1.33d;

        @Override
        public FeeUnit get(FeeRecommendationRequest request, ProjectedBlock projectedBlock) {
            // if fees should be provided for a super important tx, the value is calculated differently.
            // the default behaviour recommends a value greater than the median fee in the next block.
            if (request.isTargetDurationZeroOrLess()) {
                return calcForTargetDurationZeroOrLess(projectedBlock);
            }

            // .. otherwise, default behaviour is to just take a median fee
            BigDecimal feeRate = BigDecimal.valueOf(projectedBlock.getMedianFee());

            return SatPerVbyteImpl.builder()
                    .satPerVbyteValue(feeRate)
                    .build();
        }

        /**
         * Default behaviour of fee recommendations for "super important" transactions is
         * to use a value slightly above the median fee of the next projected block.
         *
         * @param projectedBlock the projected next block
         * @return a value slightly above median fee (max +33% of the median fee) in the block
         */
        private FeeUnit calcForTargetDurationZeroOrLess(ProjectedBlock projectedBlock) {
            double highestFeeInBlock = Optional.of(projectedBlock)
                    .filter(val -> val.getFeeRangeCount() > 0)
                    .map(val -> val.getFeeRange(val.getFeeRangeCount() - 1))
                    .orElseGet(() -> {
                        double averageFee = projectedBlock.getBlockVsize() / (double) projectedBlock.getTotalFees();
                        // if a max value cannot be determined (for whatever reason),
                        // use a slightly increased average size. average is usually higher than median.
                        return averageFee * TWENTY_PERCENT_MULTIPLIER;
                    });

            double medianFee = projectedBlock.getMedianFee();
            double medianDifferenceToHighestFee = Math.max(0, highestFeeInBlock - medianFee);

            // take the minimum of `median + 33%` and `median + ((highest - median) / 2)`
            double value = Math.min(medianFee * THIRTYTHREE_PERCENT_MULTIPLIER, medianFee + (medianDifferenceToHighestFee / 2));

            return SatPerVbyteImpl.builder()
                    .satPerVbyteValue(BigDecimal.valueOf(value))
                    .build();
        }
    }
}

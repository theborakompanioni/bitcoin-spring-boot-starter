package org.tbk.bitcoin.tool.fee.earndotcom;

import lombok.extern.slf4j.Slf4j;
import org.tbk.bitcoin.tool.fee.*;
import org.tbk.bitcoin.tool.fee.FeeRecommendationResponseImpl.FeeRecommendationImpl;
import org.tbk.bitcoin.tool.fee.FeeRecommendationResponseImpl.SatPerVbyteImpl;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Slf4j
public class EarndotcomFeeProvider extends AbstractFeeProvider {

    private static final ProviderInfo providerInfo = ProviderInfo.SimpleProviderInfo.builder()
            .name("earn.com")
            .description("")
            .build();

    private final EarndotcomApiClient client;

    public EarndotcomFeeProvider(EarndotcomApiClient client) {
        super(providerInfo);

        this.client = requireNonNull(client);
    }

    @Override
    public boolean supports(FeeRecommendationRequest request) {
        return request.getDesiredConfidence().isEmpty();
    }

    @Override
    protected Flux<FeeRecommendationResponse> requestHook(FeeRecommendationRequest request) {

        TransactionFeesSummary transactionFeesSummary = this.client.transactionFeesSummary();

        long requestedMinutes = request.getDurationTarget().toMinutes();

        // earndotcom has a minum of "30" returned for "max_minutes"
        // which is a sane thing to do because you can never be sure how long it takes for the next block to be found
        long minutes = Math.max(requestedMinutes, 30L);

        Comparator<FeesSummaryEntry> comparator = Comparator.comparingLong(FeesSummaryEntry::getMinDelay)
                .thenComparing(Comparator.comparingLong(FeesSummaryEntry::getMinFee))
                .thenComparing(Comparator.comparingLong(FeesSummaryEntry::getMaxFee).reversed());

        List<FeesSummaryEntry> eligibleEntries = transactionFeesSummary.getFeeList().stream()
                .filter(val -> val.getMaxMinutes() <= minutes)
                .sorted(comparator)
                .collect(Collectors.toList());

        final Optional<FeesSummaryEntry> summaryEntryOrEmpty;
        if (request.isTargetDurationZero()) {
            // if the special value zero take the maximum fee other pay if the requested amount
            summaryEntryOrEmpty = eligibleEntries.stream().max(comparator);
        } else {
            // otherwise take the lowest one from the eligible entries
            summaryEntryOrEmpty = eligibleEntries.stream().min(comparator);
        }

        if (summaryEntryOrEmpty.isEmpty()) {
            log.warn("no suitable estimation entries present in response for request: {}", request);
            return Flux.empty();
        }

        FeesSummaryEntry feesSummaryEntry = summaryEntryOrEmpty.orElseThrow();

        final BigDecimal satPerVbyte;
        if (request.isTargetDurationZero()) {
            // take the maximum fee other pay if the requested amount is the special value zero
            satPerVbyte = BigDecimal.valueOf((feesSummaryEntry.getMinFee() + feesSummaryEntry.getMaxFee()))
                    .divide(BigDecimal.valueOf(2), 1, RoundingMode.HALF_UP);
        } else {
            // otherwise take the min from the eligible entries
            satPerVbyte = BigDecimal.valueOf(feesSummaryEntry.getMinFee());
        }

        return Flux.just(FeeRecommendationResponseImpl.builder()
                .addFeeRecommendation(FeeRecommendationImpl.builder()
                        .feeUnit(SatPerVbyteImpl.builder()
                                .satPerVbyteValue(satPerVbyte)
                                .build())
                        .build())
                .build());
    }
}

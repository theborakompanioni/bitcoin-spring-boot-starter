package org.tbk.bitcoin.tool.fee.earndotcom;

import org.tbk.bitcoin.tool.fee.FeeRecommendationRequest;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SimpleFeeSelectionStrategy implements FeeSelectionStrategy {

    private static final Comparator<FeesSummaryEntry> comparator = Comparator
            .comparingLong(FeesSummaryEntry::getMinDelay)
            .thenComparingLong(FeesSummaryEntry::getMinFee)
            .thenComparing(Comparator.comparingLong(FeesSummaryEntry::getMaxFee).reversed());

    @Override
    public Optional<FeesSummaryEntry> select(FeeRecommendationRequest request,
                                             TransactionFeesSummary transactionFeesSummary) {

        long requestedMinutes = request.getDurationTarget().toMinutes();

        // earndotcom has a minium of "30" returned for "max_minutes"
        // which is a sane thing to do because you can never be sure how long it takes for the next block to be found
        long minutes = Math.max(requestedMinutes, 30L);

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

        return summaryEntryOrEmpty;
    }
}

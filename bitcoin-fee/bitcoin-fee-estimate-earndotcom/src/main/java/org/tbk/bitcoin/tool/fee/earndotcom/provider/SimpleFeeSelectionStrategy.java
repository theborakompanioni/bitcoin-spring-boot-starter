package org.tbk.bitcoin.tool.fee.earndotcom.provider;

import org.tbk.bitcoin.tool.fee.FeeRecommendationRequest;
import org.tbk.bitcoin.tool.fee.earndotcom.client.proto.FeesSummaryEntry;
import org.tbk.bitcoin.tool.fee.earndotcom.client.proto.TransactionFeesSummary;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class SimpleFeeSelectionStrategy implements FeeSelectionStrategy {

    private static final Comparator<FeesSummaryEntry> comparator = Comparator
            .comparingLong(FeesSummaryEntry::getMinFee)
            .thenComparingLong(FeesSummaryEntry::getMaxFee)
            .thenComparingLong(FeesSummaryEntry::getMaxMinutes)
            .thenComparingLong(FeesSummaryEntry::getMinMinutes)
            .thenComparingLong(FeesSummaryEntry::getMaxDelay)
            .thenComparingLong(FeesSummaryEntry::getMinDelay);

    @Override
    public Optional<FeesSummaryEntry> select(FeeRecommendationRequest request,
                                             TransactionFeesSummary transactionFeesSummary) {

        long requestedMinutes = request.getDurationTarget().toMinutes();

        List<FeesSummaryEntry> feeList = transactionFeesSummary.getFeeList();
        if (feeList.isEmpty()) {
            return Optional.empty();
        }
        // earndotcom has a certain minimum returned for "max_minutes" e.g. 25.
        // which is a sane thing to do because you can never be sure how long it takes for the next block to be found
        // but this means we have to filter value with this value beforehand.
        FeesSummaryEntry lastEntry = feeList.get(feeList.size() - 1);
        long minutes = Math.max(requestedMinutes, lastEntry.getMaxMinutes());

        List<FeesSummaryEntry> eligibleEntries = feeList.stream()
                .filter(val -> val.getMaxMinutes() <= minutes)
                .toList();

        final Optional<FeesSummaryEntry> summaryEntryOrEmpty;
        if (request.isTargetDurationZeroOrLess()) {
            // if the special value zero take the maximum fee other pay if the requested amount
            summaryEntryOrEmpty = eligibleEntries.stream().max(comparator);
        } else {
            // otherwise take the lowest one from the eligible entries
            summaryEntryOrEmpty = eligibleEntries.stream().min(comparator);
        }

        return summaryEntryOrEmpty;
    }
}

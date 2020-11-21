package org.tbk.bitcoin.tool.fee.earndotcom;

import org.tbk.bitcoin.tool.fee.FeeRecommendationRequest;

import java.util.Optional;

public interface FeeSelectionStrategy {

    Optional<FeesSummaryEntry> select(FeeRecommendationRequest request, TransactionFeesSummary summary);

}

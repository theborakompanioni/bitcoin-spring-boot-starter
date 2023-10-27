package org.tbk.bitcoin.tool.fee.earndotcom.provider;

import org.tbk.bitcoin.tool.fee.FeeRecommendationRequest;
import org.tbk.bitcoin.tool.fee.earndotcom.client.proto.FeesSummaryEntry;
import org.tbk.bitcoin.tool.fee.earndotcom.client.proto.TransactionFeesSummary;

import java.util.Optional;

public interface FeeSelectionStrategy {

    Optional<FeesSummaryEntry> select(FeeRecommendationRequest request, TransactionFeesSummary summary);

}

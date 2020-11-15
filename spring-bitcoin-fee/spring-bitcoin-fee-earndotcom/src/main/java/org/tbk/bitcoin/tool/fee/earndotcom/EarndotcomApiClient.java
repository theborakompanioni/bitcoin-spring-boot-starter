package org.tbk.bitcoin.tool.fee.earndotcom;

public interface EarndotcomApiClient {

    RecommendedTransactionFees recommendedTransactionFees();

    TransactionFeesSummary transactionFeesSummary();
}

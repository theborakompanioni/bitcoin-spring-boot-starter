package org.tbk.bitcoin.tool.fee.earndotcom.client;

public interface EarndotcomApiClient {

    RecommendedTransactionFees recommendedTransactionFees();

    TransactionFeesSummary transactionFeesSummary();
}

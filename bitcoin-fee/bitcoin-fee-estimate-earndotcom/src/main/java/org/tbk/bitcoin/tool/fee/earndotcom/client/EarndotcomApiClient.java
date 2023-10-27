package org.tbk.bitcoin.tool.fee.earndotcom.client;

import org.tbk.bitcoin.tool.fee.earndotcom.client.proto.RecommendedTransactionFees;
import org.tbk.bitcoin.tool.fee.earndotcom.client.proto.TransactionFeesSummary;

public interface EarndotcomApiClient {

    RecommendedTransactionFees recommendedTransactionFees();

    TransactionFeesSummary transactionFeesSummary();
}

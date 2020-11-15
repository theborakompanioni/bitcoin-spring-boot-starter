package org.tbk.bitcoin.tool.fee.bitcoinerlive;

public interface BitcoinerliveFeeApiClient {
    FeeEstimatesLatestResponse feeEstimatesLatest(FeeEstimatesLatestRequest request);
}

package org.tbk.bitcoin.tool.fee.bitcoinerlive;

import org.tbk.bitcoin.tool.fee.bitcoinerlive.proto.FeeEstimatesLatestRequest;
import org.tbk.bitcoin.tool.fee.bitcoinerlive.proto.FeeEstimatesLatestResponse;

public interface BitcoinerliveFeeApiClient {
    FeeEstimatesLatestResponse feeEstimatesLatest(FeeEstimatesLatestRequest request);
}

package org.tbk.bitcoin.tool.fee.bitcore;


import org.tbk.bitcoin.tool.fee.bitcore.proto.FeeEstimationRequest;
import org.tbk.bitcoin.tool.fee.bitcore.proto.FeeEstimationResponse;

public interface BitcoreFeeApiClient {
    FeeEstimationResponse bitcoinMainnetFee(FeeEstimationRequest request);
}

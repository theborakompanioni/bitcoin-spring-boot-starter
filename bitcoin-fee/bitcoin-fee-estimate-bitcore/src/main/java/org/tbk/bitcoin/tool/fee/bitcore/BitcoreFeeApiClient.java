package org.tbk.bitcoin.tool.fee.bitcore;


public interface BitcoreFeeApiClient {
    FeeEstimationResponse bitcoinMainnetFee(FeeEstimationRequest request);
}

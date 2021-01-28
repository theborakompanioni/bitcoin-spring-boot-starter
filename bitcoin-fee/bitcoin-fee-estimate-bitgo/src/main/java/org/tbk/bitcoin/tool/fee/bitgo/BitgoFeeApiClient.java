package org.tbk.bitcoin.tool.fee.bitgo;

public interface BitgoFeeApiClient {
    BtcTxFeeResponse btcTxFee(BtcTxFeeRequest request);
}

package org.tbk.bitcoin.tool.fee.bitgo;

import org.tbk.bitcoin.tool.fee.bitgo.proto.BtcTxFeeRequest;
import org.tbk.bitcoin.tool.fee.bitgo.proto.BtcTxFeeResponse;

public interface BitgoFeeApiClient {
    BtcTxFeeResponse btcTxFee(BtcTxFeeRequest request);
}

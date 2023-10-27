package org.tbk.bitcoin.tool.fee.btcdotcom;

import org.tbk.bitcoin.tool.fee.btcdotcom.proto.FeeDistribution;

public interface BtcdotcomFeeApiClient {
    FeeDistribution feeDistribution();
}

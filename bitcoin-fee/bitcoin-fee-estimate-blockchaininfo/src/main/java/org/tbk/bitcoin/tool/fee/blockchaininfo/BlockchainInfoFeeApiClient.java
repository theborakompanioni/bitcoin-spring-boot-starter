package org.tbk.bitcoin.tool.fee.blockchaininfo;

import org.tbk.bitcoin.tool.fee.blockchaininfo.proto.MempoolFees;

public interface BlockchainInfoFeeApiClient {

    // https://api.blockchain.info/mempool/fees
    MempoolFees mempoolFees();
}

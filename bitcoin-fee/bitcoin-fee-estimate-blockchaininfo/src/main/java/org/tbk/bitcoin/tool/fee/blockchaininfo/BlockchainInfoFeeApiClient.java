package org.tbk.bitcoin.tool.fee.blockchaininfo;

public interface BlockchainInfoFeeApiClient {

    // https://api.blockchain.info/mempool/fees
    MempoolFees mempoolFees();
}

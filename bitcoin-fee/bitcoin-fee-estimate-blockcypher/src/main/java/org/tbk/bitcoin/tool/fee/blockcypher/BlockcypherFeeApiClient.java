package org.tbk.bitcoin.tool.fee.blockcypher;


import org.tbk.bitcoin.tool.fee.blockcypher.proto.ChainInfo;

public interface BlockcypherFeeApiClient {
    ChainInfo btcMain();

    ChainInfo btcTestnet3();
}

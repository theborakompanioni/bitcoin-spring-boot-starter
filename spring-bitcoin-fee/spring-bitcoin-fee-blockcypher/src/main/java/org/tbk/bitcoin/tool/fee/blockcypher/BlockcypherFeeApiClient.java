package org.tbk.bitcoin.tool.fee.blockcypher;


public interface BlockcypherFeeApiClient {
    ChainInfo btcMain();

    ChainInfo btcTestnet3();
}

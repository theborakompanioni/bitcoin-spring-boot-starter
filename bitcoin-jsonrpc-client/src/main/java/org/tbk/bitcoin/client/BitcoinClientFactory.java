package org.tbk.bitcoin.client;

import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import com.msgilligan.bitcoinj.rpc.RpcConfig;

public interface BitcoinClientFactory {

    BitcoinClient create(RpcConfig config);

}

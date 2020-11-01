package org.tbk.bitcoin.client;

import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import com.msgilligan.bitcoinj.rpc.RpcConfig;

public final class BitcoinClientFactoryImpl implements BitcoinClientFactory {

    @Override
    public BitcoinClient create(RpcConfig config) {
        return new BitcoinClient(config);
    }
}

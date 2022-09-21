package org.tbk.bitcoin.jsonrpc;

import org.consensusj.bitcoin.jsonrpc.BitcoinClient;
import org.consensusj.bitcoin.jsonrpc.RpcConfig;

public final class BitcoinJsonRpcClientFactoryImpl implements BitcoinJsonRpcClientFactory {

    @Override
    public BitcoinClient create(RpcConfig config) {
        return new BitcoinClient(config);
    }
}

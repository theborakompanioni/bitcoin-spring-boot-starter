package org.tbk.bitcoin.jsonrpc;

import org.consensusj.bitcoin.rpc.BitcoinClient;
import org.consensusj.bitcoin.rpc.RpcConfig;

public interface BitcoinJsonRpcClientFactory {

    BitcoinClient create(RpcConfig config);

}

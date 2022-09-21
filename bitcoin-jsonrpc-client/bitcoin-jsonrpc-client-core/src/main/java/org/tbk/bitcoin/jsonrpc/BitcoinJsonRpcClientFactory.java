package org.tbk.bitcoin.jsonrpc;

import org.consensusj.bitcoin.jsonrpc.BitcoinClient;
import org.consensusj.bitcoin.jsonrpc.RpcConfig;

public interface BitcoinJsonRpcClientFactory {

    BitcoinClient create(RpcConfig config);

}

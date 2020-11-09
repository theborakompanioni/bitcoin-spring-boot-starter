package org.tbk.bitcoin.jsonrpc.cache;

public interface CacheFacade {

    TransactionCache tx();

    RawTransactionInfoCache txInfo();

    BlockCache block();

    BlockInfoCache blockInfo();

}

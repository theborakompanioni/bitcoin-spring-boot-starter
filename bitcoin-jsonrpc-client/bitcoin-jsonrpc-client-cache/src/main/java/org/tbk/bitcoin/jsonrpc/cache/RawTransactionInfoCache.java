package org.tbk.bitcoin.jsonrpc.cache;

import com.google.common.cache.ForwardingLoadingCache.SimpleForwardingLoadingCache;
import com.google.common.cache.LoadingCache;
import org.bitcoinj.core.Sha256Hash;
import org.consensusj.bitcoin.json.pojo.RawTransactionInfo;

public final class RawTransactionInfoCache extends SimpleForwardingLoadingCache<Sha256Hash, RawTransactionInfo> {
    public RawTransactionInfoCache(LoadingCache<Sha256Hash, RawTransactionInfo> delegate) {
        super(delegate);
    }
}

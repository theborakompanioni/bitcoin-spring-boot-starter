package org.tbk.bitcoin.jsonrpc.cache;

import com.google.common.cache.ForwardingLoadingCache.SimpleForwardingLoadingCache;
import com.google.common.cache.LoadingCache;
import com.msgilligan.bitcoinj.json.pojo.RawTransactionInfo;
import org.bitcoinj.core.Sha256Hash;

public final class RawTransactionInfoCache extends SimpleForwardingLoadingCache<Sha256Hash, RawTransactionInfo> {
    public RawTransactionInfoCache(LoadingCache<Sha256Hash, RawTransactionInfo> delegate) {
        super(delegate);
    }
}

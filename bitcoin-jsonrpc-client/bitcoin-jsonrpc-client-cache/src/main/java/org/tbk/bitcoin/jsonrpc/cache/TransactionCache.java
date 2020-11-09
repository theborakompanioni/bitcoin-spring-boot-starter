package org.tbk.bitcoin.jsonrpc.cache;

import com.google.common.cache.ForwardingLoadingCache.SimpleForwardingLoadingCache;
import com.google.common.cache.LoadingCache;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;

public final class TransactionCache extends SimpleForwardingLoadingCache<Sha256Hash, Transaction> {
    public TransactionCache(LoadingCache<Sha256Hash, Transaction> delegate) {
        super(delegate);
    }
}

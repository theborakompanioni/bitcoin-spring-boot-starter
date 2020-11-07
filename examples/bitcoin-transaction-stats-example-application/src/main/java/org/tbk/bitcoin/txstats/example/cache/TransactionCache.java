package org.tbk.bitcoin.txstats.example.cache;

import com.google.common.cache.ForwardingLoadingCache;
import com.google.common.cache.LoadingCache;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;

public final class TransactionCache extends ForwardingLoadingCache.SimpleForwardingLoadingCache<Sha256Hash, Transaction> {
    public TransactionCache(LoadingCache<Sha256Hash, Transaction> delegate) {
        super(delegate);
    }
}

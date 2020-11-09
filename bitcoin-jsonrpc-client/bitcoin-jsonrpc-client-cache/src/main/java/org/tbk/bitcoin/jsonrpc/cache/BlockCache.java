package org.tbk.bitcoin.jsonrpc.cache;

import com.google.common.cache.ForwardingLoadingCache.SimpleForwardingLoadingCache;
import com.google.common.cache.LoadingCache;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.Sha256Hash;

public final class BlockCache extends SimpleForwardingLoadingCache<Sha256Hash, Block> {
    public BlockCache(LoadingCache<Sha256Hash, Block> delegate) {
        super(delegate);
    }
}

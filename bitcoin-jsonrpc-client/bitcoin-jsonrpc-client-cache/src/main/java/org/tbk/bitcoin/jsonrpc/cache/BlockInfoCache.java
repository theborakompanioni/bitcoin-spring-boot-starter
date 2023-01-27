package org.tbk.bitcoin.jsonrpc.cache;

import com.google.common.cache.ForwardingLoadingCache.SimpleForwardingLoadingCache;
import com.google.common.cache.LoadingCache;
import org.bitcoinj.core.Sha256Hash;
import org.consensusj.bitcoin.json.pojo.BlockInfo;

public final class BlockInfoCache extends SimpleForwardingLoadingCache<Sha256Hash, BlockInfo> {
    public BlockInfoCache(LoadingCache<Sha256Hash, BlockInfo> delegate) {
        super(delegate);
    }
}

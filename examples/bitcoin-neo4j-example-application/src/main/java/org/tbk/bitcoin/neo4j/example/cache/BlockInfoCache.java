package org.tbk.bitcoin.neo4j.example.cache;

import com.google.common.cache.ForwardingLoadingCache;
import com.google.common.cache.LoadingCache;
import com.msgilligan.bitcoinj.json.pojo.BlockInfo;
import org.bitcoinj.core.Sha256Hash;

public final class BlockInfoCache extends ForwardingLoadingCache.SimpleForwardingLoadingCache<Sha256Hash, BlockInfo> {
    public BlockInfoCache(LoadingCache<Sha256Hash, BlockInfo> delegate) {
        super(delegate);
    }
}

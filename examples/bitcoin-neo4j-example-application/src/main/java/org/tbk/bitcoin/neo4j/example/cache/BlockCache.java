package org.tbk.bitcoin.neo4j.example.cache;

import com.google.common.cache.ForwardingLoadingCache;
import com.google.common.cache.LoadingCache;
import com.msgilligan.bitcoinj.json.pojo.BlockInfo;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.Sha256Hash;

public final class BlockCache extends ForwardingLoadingCache.SimpleForwardingLoadingCache<Sha256Hash, Block> {
    public BlockCache(LoadingCache<Sha256Hash, Block> delegate) {
        super(delegate);
    }
}

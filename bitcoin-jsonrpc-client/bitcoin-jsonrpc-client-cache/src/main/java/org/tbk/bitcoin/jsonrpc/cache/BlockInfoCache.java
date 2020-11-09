package org.tbk.bitcoin.jsonrpc.cache;

import com.google.common.cache.ForwardingLoadingCache.SimpleForwardingLoadingCache;
import com.google.common.cache.LoadingCache;
import com.msgilligan.bitcoinj.json.pojo.BlockInfo;
import org.bitcoinj.core.Sha256Hash;

public final class BlockInfoCache extends SimpleForwardingLoadingCache<Sha256Hash, BlockInfo> {
    public BlockInfoCache(LoadingCache<Sha256Hash, BlockInfo> delegate) {
        super(delegate);
    }
}

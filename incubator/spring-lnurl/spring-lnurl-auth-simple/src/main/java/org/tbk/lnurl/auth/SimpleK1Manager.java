package org.tbk.lnurl.auth;

import static java.util.Objects.requireNonNull;

public final class SimpleK1Manager implements K1Manager {

    private final K1Factory factory;
    private final K1Cache cache;

    public SimpleK1Manager(K1Factory factory, K1Cache cache) {
        this.factory = requireNonNull(factory);
        this.cache = requireNonNull(cache);
    }

    @Override
    public K1 create() {
        K1 k1 = factory.create();
        cache.put(k1);
        return k1;
    }

    @Override
    public boolean isValid(K1 k1) {
        return cache.isPresent(k1);
    }

    @Override
    public void invalidate(K1 k1) {
        cache.remove(k1);
    }
}

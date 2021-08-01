package org.tbk.lnurl.auth;

import lombok.extern.slf4j.Slf4j;
import org.tbk.lnurl.K1;
import org.tbk.lnurl.LnUrlAuth;
import org.tbk.lnurl.simple.SimpleLnUrlAuth;

import java.net.URI;

import static java.util.Objects.requireNonNull;

@Slf4j
public final class SimpleLnurlAuthFactory implements LnurlAuthFactory {

    private final URI base;

    private final K1Manager k1Manager;

    public SimpleLnurlAuthFactory(URI domain, K1Manager k1Manager) {
        this.base = requireNonNull(domain);
        this.k1Manager = requireNonNull(k1Manager);
    }

    @Override
    public LnUrlAuth createLnUrlAuth() {
        K1 k1 = k1Manager.create();
        return SimpleLnUrlAuth.create(base, k1);
    }
}

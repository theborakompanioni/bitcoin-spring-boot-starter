package org.tbk.lnurl.auth;

import org.tbk.lnurl.simple.auth.SimpleK1;

public final class SimpleK1Factory implements K1Factory {
    @Override
    public K1 create() {
        return SimpleK1.random();
    }
}

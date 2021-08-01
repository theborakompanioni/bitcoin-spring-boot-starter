package org.tbk.lnurl.auth;

import org.tbk.lnurl.K1;
import org.tbk.lnurl.simple.SimpleK1;

public final class SimpleK1Factory implements K1Factory {
    @Override
    public K1 create() {
        return SimpleK1.random();
    }
}

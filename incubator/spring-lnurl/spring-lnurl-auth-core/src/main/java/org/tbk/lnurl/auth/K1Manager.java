package org.tbk.lnurl.auth;

public interface K1Manager extends K1Factory {

    boolean isValid(K1 k1);

    void invalidate(K1 k1);
}

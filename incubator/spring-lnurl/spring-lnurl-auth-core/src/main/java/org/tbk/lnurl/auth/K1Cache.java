package org.tbk.lnurl.auth;

import org.tbk.lnurl.K1;

import java.time.Duration;

public interface K1Cache {

    void put(K1 k1);

    boolean isPresent(K1 k1);

    void remove(K1 k1);
}

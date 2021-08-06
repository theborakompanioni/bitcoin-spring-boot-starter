package org.tbk.lnurl.test;


import org.tbk.lnurl.auth.K1;
import org.tbk.lnurl.simple.auth.SimpleK1;

import java.security.SecureRandom;

public final class K1TestUtils {

    private static final SecureRandom RANDOM = new SecureRandom();

    public static K1 random() {
        byte[] bytes = new byte[32];

        RANDOM.nextBytes(bytes);

        return SimpleK1.fromBytes(bytes);
    }

    private K1TestUtils() {
        throw new UnsupportedOperationException();
    }
}

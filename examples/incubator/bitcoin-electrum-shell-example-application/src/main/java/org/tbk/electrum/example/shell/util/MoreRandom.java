package org.tbk.electrum.example.shell.util;

import java.security.SecureRandom;

public final class MoreRandom {
    private static final SecureRandom RANDOM = new SecureRandom();

    private MoreRandom() {
        throw new UnsupportedOperationException();
    }

    public static byte[] randomByteArray(int len) {
        byte[] bytes = new byte[len];
        RANDOM.nextBytes(bytes);
        return bytes;
    }
}

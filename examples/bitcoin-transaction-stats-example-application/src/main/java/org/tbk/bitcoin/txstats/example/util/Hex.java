package org.tbk.bitcoin.txstats.example.util;

import com.google.common.io.BaseEncoding;

public final class Hex {
    private static final BaseEncoding base16 = BaseEncoding.base16().lowerCase();

    private Hex() {
        throw new UnsupportedOperationException();
    }

    public static String encode(byte[] raw) {
        return base16.encode(raw);
    }

    public static byte[] decode(String hex) {
        return base16.decode(hex.toLowerCase());
    }

}

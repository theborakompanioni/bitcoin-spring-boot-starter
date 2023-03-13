package org.tbk.bitcoin.common.util;

import java.util.HexFormat;

/**
 * @deprecated Use {@link HexFormat} instead. Scheduled for removal in v0.9.0.
 */
@Deprecated
public final class Hex {
    private static final HexFormat base16 = HexFormat.of();

    private Hex() {
        throw new UnsupportedOperationException();
    }

    public static String encode(byte[] raw) {
        return base16.formatHex(raw);
    }

    public static byte[] decode(String hex) {
        return base16.parseHex(hex.toLowerCase());
    }

}

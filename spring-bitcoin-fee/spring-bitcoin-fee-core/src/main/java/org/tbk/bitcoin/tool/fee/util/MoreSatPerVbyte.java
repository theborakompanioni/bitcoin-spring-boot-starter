package org.tbk.bitcoin.tool.fee.util;

import java.math.BigDecimal;

public final class MoreSatPerVbyte {
    private static final int KILO_SCALE = -3;

    private MoreSatPerVbyte() {
        throw new UnsupportedOperationException();
    }

    public static BigDecimal fromBtcPerKVbyte(BigDecimal btcPerKVbyte) {
        // this approach makes sure to never decrease the scale of the provided value
        // DO CHANGE ONLY IF YOU KNOW WHAT YOU ARE DOING
        // see {@see MoreSatPerVbyteTest}
        return btcPerKVbyte.multiply(BigDecimal.ONE.scaleByPowerOfTen(MoreBitcoin.fractionDigits()).movePointRight(-3));
    }

    public static BigDecimal fromBtcPerVbyte(BigDecimal btcPerVbyte) {
        // this approach makes sure to never decrease the scale of the provided value
        // DO CHANGE ONLY IF YOU KNOW WHAT YOU ARE DOING
        // see {@see MoreSatPerVbyteTest}
        return btcPerVbyte.multiply(MoreBitcoin.btcToSatFactor());
    }

    public static BigDecimal fromSatPerKVbyte(BigDecimal satPerKilobyte) {
        // this approach makes sure to never decrease the scale of the provided value
        // DO CHANGE ONLY IF YOU KNOW WHAT YOU ARE DOING
        // see {@see MoreSatPerVbyteTest}
        return satPerKilobyte.multiply(BigDecimal.ONE.scaleByPowerOfTen(-3));
    }
}

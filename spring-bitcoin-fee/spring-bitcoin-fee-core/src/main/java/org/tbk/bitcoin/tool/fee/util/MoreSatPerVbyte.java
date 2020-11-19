package org.tbk.bitcoin.tool.fee.util;

import java.math.BigDecimal;

public final class MoreSatPerVbyte {
    private static final int KILO_SCALE = -3;

    private MoreSatPerVbyte() {
        throw new UnsupportedOperationException();
    }

    public static BigDecimal fromBtcPerKVbyte(BigDecimal btcPerKVbyte) {
        //return fromSatPerKVbyte(btcPerKVbyte.multiply(MoreBitcoin.btcToSatFactor()));
        //return fromSatPerKVbyte(btcPerKVbyte.scaleByPowerOfTen(MoreBitcoin.fractionDigits()));
        //return fromSatPerKVbyte(btcPerKVbyte.movePointRight(MoreBitcoin.fractionDigits()));
        //return btcPerKVbyte.multiply(BigDecimal.ONE.scaleByPowerOfTen(5));
        //return btcPerKVbyte.movePointRight(5);
        //fromSatPerKVbyte(btcPerKVbyte.multiply(MoreBitcoin.btcToSatFactor()));
        //return btcPerKVbyte.multiply(BigDecimal.TEN.pow(MoreBitcoin.fractionDigits() - 3));
        return btcPerKVbyte.multiply(BigDecimal.ONE.scaleByPowerOfTen(MoreBitcoin.fractionDigits()).movePointRight(-3));
    }

    public static BigDecimal fromBtcPerVbyte(BigDecimal btcPerVbyte) {
        //return btcPerVbyte.scaleByPowerOfTen(MoreBitcoin.fractionDigits());
        //return btcPerVbyte.movePointRight(MoreBitcoin.fractionDigits());
        //return btcPerVbyte.multiply(BigDecimal.ONE.scaleByPowerOfTen(MoreBitcoin.fractionDigits()));
        return btcPerVbyte.multiply(MoreBitcoin.btcToSatFactor());
    }

    public static BigDecimal fromSatPerKVbyte(BigDecimal satPerKilobyte) {
        //return satPerKilobyte.divide(BigDecimal.valueOf(1_000L));
        //return satPerKilobyte.scaleByPowerOfTen(-3);
        return satPerKilobyte.multiply(BigDecimal.ONE.scaleByPowerOfTen(-3));
    }
}

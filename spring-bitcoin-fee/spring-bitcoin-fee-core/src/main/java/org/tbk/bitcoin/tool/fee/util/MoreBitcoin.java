package org.tbk.bitcoin.tool.fee.util;

import java.math.BigDecimal;
import java.time.Duration;

public final class MoreBitcoin {
    private static final Duration AVERAGE_BLOCK_DURATION = Duration.ofMinutes(10);
    private static final int FRACTION_DIGITS = 8;
    private static final BigDecimal BTC_TO_SAT_FACTOR = BigDecimal.TEN.pow(FRACTION_DIGITS);

    private MoreBitcoin() {
        throw new UnsupportedOperationException();
    }

    public static Duration averageBlockDuration() {
        return averageBlockDuration(1);
    }

    public static Duration averageBlockDuration(long numBlocks) {
        return AVERAGE_BLOCK_DURATION.multipliedBy(numBlocks);
    }

    public static BigDecimal btcToSatFactor() {
        return BTC_TO_SAT_FACTOR;
    }

    public static int fractionDigits() {
        return FRACTION_DIGITS;
    }
}

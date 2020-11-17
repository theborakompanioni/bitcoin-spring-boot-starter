package org.tbk.bitcoin.tool.fee.util;

import java.time.Duration;

public final class MoreBitcoin {
    private static final Duration AVERAGE_BLOCK_DURATION = Duration.ofMinutes(10);
    ;

    private MoreBitcoin() {
        throw new UnsupportedOperationException();
    }

    public static Duration averageBlockDuration() {
        return AVERAGE_BLOCK_DURATION;
    }
}

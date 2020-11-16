package org.tbk.bitcoin.tool.fee.util;

import java.time.Duration;

public final class MoreBitcoin {

    private MoreBitcoin() {
        throw new UnsupportedOperationException();
    }

    public static Duration averageBlockDuration() {
        return Duration.ofMinutes(10);
    }
}

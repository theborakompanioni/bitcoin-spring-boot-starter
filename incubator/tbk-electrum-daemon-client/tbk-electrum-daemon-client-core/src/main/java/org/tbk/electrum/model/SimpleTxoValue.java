package org.tbk.electrum.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SimpleTxoValue implements TxoValue {
    private static final SimpleTxoValue ZERO = new SimpleTxoValue(0L);
    private static final SimpleTxoValue ONE = new SimpleTxoValue(1L);

    public static TxoValue zero() {
        return ZERO;
    }

    public static TxoValue one() {
        return ONE;
    }

    public static TxoValue of(long value) {
        if (value == 0L) {
            return ZERO;
        }
        return new SimpleTxoValue(value);
    }

    long value;

    @Override
    public boolean isZero() {
        return this == ZERO;
    }
}

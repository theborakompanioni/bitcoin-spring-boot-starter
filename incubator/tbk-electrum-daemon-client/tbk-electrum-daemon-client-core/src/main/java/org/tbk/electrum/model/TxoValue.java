package org.tbk.electrum.model;

/**
 * Representing a value of a transaction output.
 *
 * <p>This value can also be negative.
 *
 * <p>e.g. Electrum uses negative values for "outgoing" transactions in its "history" response.
 */
public interface TxoValue {
    long getValue();

    default boolean isZero() {
        return getValue() == 0L;
    }
}

package org.tbk.electrum.model;

import org.junit.jupiter.api.Test;

import java.security.SecureRandom;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class SimpleTxoValueTest {

    @Test
    void zero() {
        TxoValue zero = SimpleTxoValue.zero();
        assertThat(zero.getValue(), is(0L));
        assertThat(zero.isZero(), is(true));

        TxoValue zero2 = SimpleTxoValue.of(0L);
        assertThat(zero2.getValue(), is(0L));
        assertThat(zero2.isZero(), is(true));
        assertThat(zero2, is(zero));
    }

    @Test
    void canHavePositiveValue() {
        long value = Math.abs(new SecureRandom().nextLong() + 1L);

        TxoValue txValue = SimpleTxoValue.of(value);
        assertThat(txValue.getValue(), is(greaterThanOrEqualTo(1L)));
        assertThat(txValue.isZero(), is(false));
    }

    @Test
    void canHaveNegativeValue() {
        long value = -1L * Math.abs(new SecureRandom().nextLong() + 1L);

        TxoValue txValue = SimpleTxoValue.of(value);
        assertThat(txValue.getValue(), is(lessThanOrEqualTo(-1L)));
        assertThat(txValue.isZero(), is(false));
    }
}

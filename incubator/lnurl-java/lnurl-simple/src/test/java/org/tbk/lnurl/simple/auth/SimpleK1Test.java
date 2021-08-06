package org.tbk.lnurl.simple.auth;

import org.junit.jupiter.api.Test;
import org.tbk.lnurl.simple.auth.SimpleK1;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SimpleK1Test {

    @Test
    void fromHexSuccessful() {
        SimpleK1 k1 = SimpleK1.fromHex("00".repeat(32));
        assertThat(k1.toHex(), is("0000000000000000000000000000000000000000000000000000000000000000"));
        assertThat(k1.toArray().length, is(32));
        assertThat(k1, is(new SimpleK1(new byte[32])));
    }

    @Test
    void fromHexFail() {
        assertThrows(NullPointerException.class, () -> SimpleK1.fromHex(null));
        assertThrows(IllegalArgumentException.class, () -> SimpleK1.fromHex(""));

        IllegalArgumentException e1 = assertThrows(IllegalArgumentException.class, () -> SimpleK1.fromHex("00"));
        assertThat(e1.getMessage(), is("data must be an array of size 32"));

        IllegalArgumentException e2 = assertThrows(IllegalArgumentException.class, () -> SimpleK1.fromHex("00".repeat(33)));
        assertThat(e2.getMessage(), is(e1.getMessage()));
    }
}
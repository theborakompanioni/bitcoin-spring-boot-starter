package org.tbk.bitcoin.tool.fee.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class MoreSatPerVbyteTest {

    @Test
    public void itShouldCalculateSatPerKbyteToSatPerVbyte() {
        assertThat(MoreSatPerVbyte.fromSatPerKVbyte(new BigDecimal("0.00000001")), is(new BigDecimal("0.00000000001")));
        assertThat(MoreSatPerVbyte.fromSatPerKVbyte(new BigDecimal("0.99999999")), is(new BigDecimal("0.00099999999")));
        assertThat(MoreSatPerVbyte.fromSatPerKVbyte(new BigDecimal("1")), is(new BigDecimal("0.001")));
        assertThat(MoreSatPerVbyte.fromSatPerKVbyte(new BigDecimal("1.0")), is(new BigDecimal("0.0010")));
        assertThat(MoreSatPerVbyte.fromSatPerKVbyte(new BigDecimal("1.00000000")), is(new BigDecimal("0.00100000000")));
        assertThat(MoreSatPerVbyte.fromSatPerKVbyte(new BigDecimal("1.00000001")), is(new BigDecimal("0.00100000001")));
        assertThat(MoreSatPerVbyte.fromSatPerKVbyte(new BigDecimal("1000.000")), is(new BigDecimal("1.000000")));
    }

    @Test
    public void itShouldCalculateBtcPerVbyteToSatPerVbyte() {
        assertThat(MoreSatPerVbyte.fromBtcPerVbyte(new BigDecimal("0.00000001")), is(new BigDecimal("1.00000000")));
        assertThat(MoreSatPerVbyte.fromBtcPerVbyte(new BigDecimal("0.99999999")), is(new BigDecimal("99999999.00000000")));
        assertThat(MoreSatPerVbyte.fromBtcPerVbyte(new BigDecimal("1")), is(new BigDecimal("100000000")));
        assertThat(MoreSatPerVbyte.fromBtcPerVbyte(new BigDecimal("1.0")), is(new BigDecimal("100000000.0")));
        assertThat(MoreSatPerVbyte.fromBtcPerVbyte(new BigDecimal("1.00000000")), is(new BigDecimal("100000000.00000000")));
        assertThat(MoreSatPerVbyte.fromBtcPerVbyte(new BigDecimal("1.00000001")), is(new BigDecimal("100000001.00000000")));
        assertThat(MoreSatPerVbyte.fromBtcPerVbyte(new BigDecimal("1000.000")), is(new BigDecimal("100000000000.000")));
    }

    @Test
    public void itShouldCalculateBtcPerKVbyteToSatPerVbyte() {
        assertThat(MoreSatPerVbyte.fromBtcPerKVbyte(new BigDecimal("0.00000001")), is(new BigDecimal("0.00100000")));
        assertThat(MoreSatPerVbyte.fromBtcPerKVbyte(new BigDecimal("0.00001000")), is(new BigDecimal("1.00000000")));
        assertThat(MoreSatPerVbyte.fromBtcPerKVbyte(new BigDecimal("0.99999999")), is(new BigDecimal("99999.99900000")));
        assertThat(MoreSatPerVbyte.fromBtcPerKVbyte(new BigDecimal("1")), is(new BigDecimal("100000")));
        assertThat(MoreSatPerVbyte.fromBtcPerKVbyte(new BigDecimal("1.0")), is(new BigDecimal("100000.0")));
        assertThat(MoreSatPerVbyte.fromBtcPerKVbyte(new BigDecimal("1.00000000")), is(new BigDecimal("100000.00000000")));
        assertThat(MoreSatPerVbyte.fromBtcPerKVbyte(new BigDecimal("1.00001")), is(new BigDecimal("100001.00000")));
        assertThat(MoreSatPerVbyte.fromBtcPerKVbyte(new BigDecimal("1000.000")), is(new BigDecimal("100000000.000")));
    }

}

package org.tbk.bitcoin.tool.fee.util;

import org.junit.Test;

import java.math.BigDecimal;
import java.time.Duration;

import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class MoreBitcoinTest {

    @Test
    public void itShouldReturnAverageBlockDuration() {
        assertThat("average block duration is 10 minutes", MoreBitcoin.averageBlockDuration(), is(Duration.ofMinutes(10)));
    }

    @Test
    public void itShouldReturnBtcToSatFactor() {
        assertThat("the btc to sat factor is correct", MoreBitcoin.btcToSatFactor(), comparesEqualTo(BigDecimal.valueOf(100_000_000L)));
    }

}

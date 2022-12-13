package org.tbk.bitcoin.tool.fee.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.is;

class MoreBitcoinTest {

    @Test
    void itShouldReturnAverageBlockDuration() {
        assertThat("average block duration is 10 minutes", MoreBitcoin.averageBlockDuration(), is(Duration.ofMinutes(10)));
    }

    @Test
    void itShouldReturnBtcToSatFactor() {
        assertThat("the btc to sat factor is correct", MoreBitcoin.btcToSatFactor(), comparesEqualTo(BigDecimal.valueOf(100_000_000L)));
    }

}

package org.tbk.bitcoin.tool.fee.blockchair;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class BlockchairFeeApiClientImplTest {
    private static final String BASE_URL = "https://api.blockchair.com";
    private static final String API_TOKEN = null;

    private BlockchairFeeApiClientImpl sut;

    @Before
    public void setUp() {
        this.sut = new BlockchairFeeApiClientImpl(BASE_URL, API_TOKEN);
    }

    @Test
    public void itShouldGetBitcoinStatsFeesOnly() {
        BitcoinStatsFeesOnly bitcoinStatsFeesOnly = this.sut.bitcoinStatsFeesOnly();

        assertThat(bitcoinStatsFeesOnly, is(notNullValue()));

        BitcoinStatsDataFeesOnly data = bitcoinStatsFeesOnly.getData();

        assertThat(data, is(notNullValue()));
        assertThat(data.getSuggestedTransactionFeePerByteSat(), is(greaterThanOrEqualTo(0L)));
        assertThat(data.getMemoolTransactions(), is(greaterThanOrEqualTo(0L)));
        assertThat(data.getMempoolSize(), is(greaterThanOrEqualTo(0L)));
        assertThat(data.getAverageTransactionFee24H(), is(greaterThanOrEqualTo(0L)));
        assertThat(data.getMedianTransactionFee24H(), is(greaterThanOrEqualTo(0L)));
        assertThat(data.getMempoolTps(), is(greaterThanOrEqualTo(0d)));
    }
}

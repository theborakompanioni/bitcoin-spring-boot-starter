package org.tbk.bitcoin.tool.fee.blockchaininfo;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class BlockchainInfoFeeApiClientImplTest {

    private static final String BASE_URL = "https://api.blockchain.info";
    private static final String API_TOKEN = "test";

    private BlockchainInfoFeeApiClientImpl sut;

    @Before
    public void setUp() {
        this.sut = new BlockchainInfoFeeApiClientImpl(BASE_URL, API_TOKEN);
    }

    @Test
    public void itShouldGetRecommendedTransactionFees() {
        MempoolFees mempoolFees = this.sut.mempoolFees();

        assertThat(mempoolFees, is(notNullValue()));
        assertThat(mempoolFees.getPriority(), is(greaterThanOrEqualTo(0L)));
        assertThat(mempoolFees.getRegular(), is(greaterThanOrEqualTo(0L)));
        assertThat(mempoolFees.getLimit(), is(notNullValue()));
        assertThat(mempoolFees.getLimit().getMin(), is(greaterThanOrEqualTo(0L)));
        assertThat(mempoolFees.getLimit().getMax(), is(greaterThanOrEqualTo(0L)));
    }
}

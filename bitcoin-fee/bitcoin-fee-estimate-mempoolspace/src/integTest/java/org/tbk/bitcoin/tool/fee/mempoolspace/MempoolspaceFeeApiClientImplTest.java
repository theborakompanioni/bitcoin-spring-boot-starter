package org.tbk.bitcoin.tool.fee.mempoolspace;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class MempoolspaceFeeApiClientImplTest {
    private static final String BASE_URL = "https://mempool.space";
    private static final String API_TOKEN = null;

    private MempoolspaceFeeApiClientImpl sut;

    @Before
    public void setUp() {
        this.sut = new MempoolspaceFeeApiClientImpl(BASE_URL, API_TOKEN);
    }

    @Test
    public void itShouldGetFeesRecommended() {
        FeesRecommended feesRecommended = this.sut.feesRecommended();

        assertThat(feesRecommended, is(notNullValue()));
        assertThat(feesRecommended.getFastestFee(), is(greaterThanOrEqualTo(0L)));
        assertThat(feesRecommended.getHalfHourFee(), is(greaterThanOrEqualTo(0L)));
        assertThat(feesRecommended.getHourFee(), is(greaterThanOrEqualTo(0L)));
    }

    @Test
    public void itShouldGetProjectedBlocks() {
        ProjectedMempoolBlocks projectedBlocks = this.sut.projectedBlocks();

        assertThat(projectedBlocks, is(notNullValue()));
        assertThat(projectedBlocks.getBlocksList(), hasSize(greaterThanOrEqualTo(0)));
    }
}

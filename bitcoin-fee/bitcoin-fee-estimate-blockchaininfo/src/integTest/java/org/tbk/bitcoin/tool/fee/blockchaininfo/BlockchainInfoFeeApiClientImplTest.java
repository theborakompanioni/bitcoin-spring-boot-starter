package org.tbk.bitcoin.tool.fee.blockchaininfo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tbk.bitcoin.tool.fee.blockchaininfo.proto.MempoolFees;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class BlockchainInfoFeeApiClientImplTest {

    private static final String BASE_URL = "https://api.blockchain.info";
    private static final String API_TOKEN = "test";

    private BlockchainInfoFeeApiClientImpl sut;

    @BeforeEach
    void setUp() {
        this.sut = new BlockchainInfoFeeApiClientImpl(BASE_URL, API_TOKEN);
    }

    @Test
    void itShouldGetRecommendedTransactionFees() {
        MempoolFees mempoolFees = this.sut.mempoolFees();

        assertThat(mempoolFees, is(notNullValue()));
        assertThat(mempoolFees.getPriority(), is(greaterThanOrEqualTo(0L)));
        assertThat(mempoolFees.getRegular(), is(greaterThanOrEqualTo(0L)));
        assertThat(mempoolFees.getLimit(), is(notNullValue()));
        assertThat(mempoolFees.getLimit().getMin(), is(greaterThanOrEqualTo(0L)));
        assertThat(mempoolFees.getLimit().getMax(), is(greaterThanOrEqualTo(0L)));
    }
}

package org.tbk.bitcoin.tool.fee.blockstreaminfo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class BlockstreamInfoFeeApiClientImplTest {
    private static final String BASE_URL = "https://blockstream.info";
    private static final String API_TOKEN = "test";

    private BlockstreamInfoFeeApiClientImpl sut;

    @BeforeEach
    void setUp() {
        this.sut = new BlockstreamInfoFeeApiClientImpl(BASE_URL, API_TOKEN);
    }

    @Test
    void itShouldGetFeeEstimates() {
        FeeEstimates feeEstimates = this.sut.feeEstimates();

        assertThat(feeEstimates, is(notNullValue()));

        List<FeeEstimates.Entry> entries = feeEstimates.getEntryList();
        assertThat(entries, hasSize(greaterThan(0)));
    }
}

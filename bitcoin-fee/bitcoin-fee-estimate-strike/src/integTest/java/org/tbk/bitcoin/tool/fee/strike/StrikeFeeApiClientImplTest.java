package org.tbk.bitcoin.tool.fee.strike;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tbk.bitcoin.tool.fee.strike.proto.BlendedFeeEstimateResponse;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class StrikeFeeApiClientImplTest {
    private static final String BASE_URL = "https://bitcoinchainfees.strike.me";

    private StrikeFeeApiClient sut;

    @BeforeEach
    void setUp() {
        this.sut = new StrikeFeeApiClientImpl(BASE_URL);
    }

    @Test
    void itShouldGetFeeEstimatesLatest() {
        BlendedFeeEstimateResponse response = this.sut.feeEstimates();

        assertThat(response, is(notNullValue()));
        assertThat(response.getCurrentBlockHeight(), is(greaterThan(0L)));
        assertThat(response.getCurrentBlockHash(), is(startsWith("0")));

        Map<String, Long> estimates = response.getFeeByBlockTargetMap();
        assertThat(estimates, is(notNullValue()));

        // might only include a single estimate
        assertThat(estimates.size(), is(greaterThanOrEqualTo(1)));
        assertThat(estimates.get("1"), is(greaterThanOrEqualTo(1_000L)));

        for (Long fee : estimates.values()) {
            assertThat(fee, is(greaterThanOrEqualTo(1_000L)));
        }
    }
}

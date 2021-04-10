package org.tbk.bitcoin.tool.fee.bitcoinerlive;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tbk.bitcoin.tool.fee.bitcoinerlive.FeeEstimatesLatestResponse.Estimate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.tbk.bitcoin.tool.fee.bitcoinerlive.FeeEstimatesLatestRequest.Confidence;

public class BitcoinerliveFeeApiClientImplTest {
    private static final String BASE_URL = "https://bitcoiner.live";
    private static final String API_TOKEN = null;

    private BitcoinerliveFeeApiClientImpl sut;

    @BeforeEach
    public void setUp() {
        this.sut = new BitcoinerliveFeeApiClientImpl(BASE_URL, API_TOKEN);
    }

    @Test
    public void itShouldGetFeeEstimatesLatest() {
        List<Confidence> confidences = Arrays.stream(Confidence.values())
                .filter(val -> val != Confidence.UNRECOGNIZED)
                .collect(Collectors.toUnmodifiableList());

        List<FeeEstimatesLatestRequest> requests = confidences.stream()
                .map(val -> FeeEstimatesLatestRequest.newBuilder()
                        .setConfidenceType(val)
                        .build())
                .collect(Collectors.toList());

        for (FeeEstimatesLatestRequest request : requests) {
            FeeEstimatesLatestResponse response = this.sut.feeEstimatesLatest(request);

            assertThat(response, is(notNullValue()));
            assertThat(response.getTimestamp(), is(greaterThan(0L)));

            Map<String, Estimate> estimates = response.getEstimateMap();
            assertThat(estimates, is(notNullValue()));
            assertThat(estimates.size(), is(greaterThanOrEqualTo(7)));

            assertThat(estimates.get("30"), is(notNullValue()));
            assertThat(estimates.get("60"), is(notNullValue()));
            assertThat(estimates.get("120"), is(notNullValue()));
            assertThat(estimates.get("180"), is(notNullValue()));
            assertThat(estimates.get("720"), is(notNullValue()));
            assertThat(estimates.get("1440"), is(notNullValue()));

            for (Estimate estimate : estimates.values()) {
                assertThat(estimate.getSatPerVbyte(), is(greaterThanOrEqualTo(0L)));

                Map<String, Estimate.Entry> total = estimate.getTotalMap();
                assertThat(total, is(notNullValue()));
                assertThat(total.size(), is(greaterThanOrEqualTo(3)));

                assertThat(total.get("p2wpkh"), is(notNullValue()));
                assertThat(total.get("p2pkh"), is(notNullValue()));
                assertThat(total.get("p2sh-p2wpkh"), is(notNullValue()));

                for (Estimate.Entry entry : total.values()) {
                    assertThat(entry.getUsd(), is(greaterThanOrEqualTo(0d)));
                    assertThat(entry.getSatoshi(), is(greaterThanOrEqualTo(0L)));

                }
            }
        }
    }
}

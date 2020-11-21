package org.tbk.bitcoin.tool.fee.earndotcom.client;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class EarndotcomApiClientImplTest {
    private static final String BASE_URL = "https://bitcoinfees.earn.com";
    private static final String API_TOKEN = "test";

    private EarndotcomApiClientImpl sut;

    @Before
    public void setUp() {
        this.sut = new EarndotcomApiClientImpl(BASE_URL, API_TOKEN);
    }

    @Test
    public void itShouldGetRecommendedTransactionFees() {
        RecommendedTransactionFees recommendedTransactionFees = this.sut.recommendedTransactionFees();

        assertThat(recommendedTransactionFees, is(notNullValue()));
        assertThat(recommendedTransactionFees.getFastestFee(), is(greaterThan(0L)));
        assertThat(recommendedTransactionFees.getHalfHourFee(), is(greaterThan(0L)));
        assertThat(recommendedTransactionFees.getHourFee(), is(greaterThan(0L)));
    }

    @Test
    public void itShouldGetTransactionFeesSummary() {
        TransactionFeesSummary transactionFeesSummary = this.sut.transactionFeesSummary();

        assertThat(transactionFeesSummary, is(notNullValue()));
        assertThat(transactionFeesSummary.getFeeList(), hasSize(greaterThan(0)));

        FeesSummaryEntry firstEntry = transactionFeesSummary.getFeeList().stream()
                .findFirst().orElseThrow();

        assertThat(firstEntry.getMinFee(), is(greaterThanOrEqualTo(0L)));
        assertThat(firstEntry.getMaxFee(), is(greaterThanOrEqualTo(0L)));
        assertThat(firstEntry.getMinMinutes(), is(greaterThanOrEqualTo(0L)));
        assertThat(firstEntry.getMaxMinutes(), is(greaterThanOrEqualTo(0L)));
        assertThat(firstEntry.getMinDelay(), is(greaterThanOrEqualTo(0L)));
        assertThat(firstEntry.getMaxDelay(), is(greaterThanOrEqualTo(0L)));
        assertThat(firstEntry.getDayCount(), is(greaterThanOrEqualTo(0L)));
        assertThat(firstEntry.getMemCount(), is(greaterThanOrEqualTo(0L)));
    }
}

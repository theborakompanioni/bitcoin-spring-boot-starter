package org.tbk.bitcoin.tool.fee.bitgo;

import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class BitgoFeeApiClientImplTest {
    private static final String BASE_URL = "https://www.bitgo.com";
    private static final String API_TOKEN = null;

    private BitgoFeeApiClientImpl sut;

    @Before
    public void setUp() {
        this.sut = new BitgoFeeApiClientImpl(BASE_URL, API_TOKEN);
    }

    @Test
    public void itShouldGetBtcTxFee() {
        BtcTxFeeRequest request = BtcTxFeeRequest.newBuilder()
                .build();

        BtcTxFeeResponse response = this.sut.btcTxFee(request);

        assertThat(response, is(notNullValue()));
        assertThat(response.getFeePerKb(), is(greaterThanOrEqualTo(0L)));
        assertThat(response.getCpfpFeePerKb(), is(greaterThanOrEqualTo(0L)));
        assertThat(response.getNumBlocks(), is(greaterThanOrEqualTo(0L)));
        assertThat(response.getConfidence(), is(greaterThanOrEqualTo(0L)));
        assertThat(response.getMultiplier(), is(greaterThanOrEqualTo(0L)));

        Map<String, Long> feeByBlockTargetMap = response.getFeeByBlockTargetMap();
        assertThat(feeByBlockTargetMap, is(notNullValue()));
        assertThat(feeByBlockTargetMap.values(), hasSize(greaterThan(5)));

        assertThat(feeByBlockTargetMap.get("1"), is(greaterThanOrEqualTo(0L)));
        assertThat(feeByBlockTargetMap.get("2"), is(greaterThanOrEqualTo(0L)));
        assertThat(feeByBlockTargetMap.get("3"), is(greaterThanOrEqualTo(0L)));
        assertThat(feeByBlockTargetMap.get("4"), is(greaterThanOrEqualTo(0L)));
        assertThat(feeByBlockTargetMap.get("5"), is(greaterThanOrEqualTo(0L)));
    }

    @Test
    public void itShouldGetBtcTxFeeWitCustomBlockNums() {
        BtcTxFeeRequest request = BtcTxFeeRequest.newBuilder()
                .setNumBlocks(10)
                .build();

        BtcTxFeeResponse response = this.sut.btcTxFee(request);

        assertThat(response, is(notNullValue()));
        assertThat(response.getNumBlocks(), is(request.getNumBlocks()));

        assertThat(response.getFeePerKb(), is(greaterThanOrEqualTo(0L)));
        assertThat(response.getCpfpFeePerKb(), is(greaterThanOrEqualTo(0L)));
        assertThat(response.getConfidence(), is(greaterThanOrEqualTo(0L)));
        assertThat(response.getMultiplier(), is(greaterThanOrEqualTo(0L)));
    }
}

package org.tbk.bitcoin.tool.fee.bitgo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tbk.bitcoin.tool.fee.bitgo.proto.BtcTxFeeRequest;
import org.tbk.bitcoin.tool.fee.bitgo.proto.BtcTxFeeResponse;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class BitgoFeeApiClientImplTest {
    private static final String BASE_URL = "https://www.bitgo.com";
    private static final String API_TOKEN = null;

    private BitgoFeeApiClientImpl sut;

    @BeforeEach
    void setUp() {
        this.sut = new BitgoFeeApiClientImpl(BASE_URL, API_TOKEN);
    }

    @Test
    void itShouldGetBtcTxFee() {
        BtcTxFeeRequest request = BtcTxFeeRequest.newBuilder()
                .build();

        BtcTxFeeResponse response = this.sut.btcTxFee(request);

        assertThat(response, is(notNullValue()));
        assertThat(response.getFeePerKb(), is(greaterThanOrEqualTo(0L)));
        assertThat(response.getCpfpFeePerKb(), is(greaterThanOrEqualTo(0L)));
        assertThat(response.getNumBlocks(), is(greaterThanOrEqualTo(0L)));
        assertThat(response.getConfidence(), is(greaterThanOrEqualTo(0L)));
        assertThat(response.getMultiplier(), is(greaterThanOrEqualTo(0d)));

        Map<String, Long> feeByBlockTargetMap = response.getFeeByBlockTargetMap();
        assertThat(feeByBlockTargetMap, is(notNullValue()));

        for (Long value : feeByBlockTargetMap.values()) {
            assertThat(value, is(greaterThanOrEqualTo(0L)));
        }
    }

    @Test
    void itShouldGetBtcTxFeeWitCustomBlockNums() {
        BtcTxFeeRequest request = BtcTxFeeRequest.newBuilder()
                .setNumBlocks(10)
                .build();

        BtcTxFeeResponse response = this.sut.btcTxFee(request);

        assertThat(response, is(notNullValue()));
        assertThat(response.getNumBlocks(), is(request.getNumBlocks()));

        assertThat(response.getFeePerKb(), is(greaterThanOrEqualTo(0L)));
        assertThat(response.getCpfpFeePerKb(), is(greaterThanOrEqualTo(0L)));
        assertThat(response.getConfidence(), is(greaterThanOrEqualTo(0L)));
        assertThat(response.getMultiplier(), is(greaterThanOrEqualTo(0d)));

        Map<String, Long> feeByBlockTargetMap = response.getFeeByBlockTargetMap();
        assertThat(feeByBlockTargetMap, is(notNullValue()));

        for (Long value : feeByBlockTargetMap.values()) {
            assertThat(value, is(greaterThanOrEqualTo(0L)));
        }
    }
}

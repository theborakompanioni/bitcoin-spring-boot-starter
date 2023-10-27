package org.tbk.bitcoin.tool.fee.bitcore;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tbk.bitcoin.tool.fee.bitcore.proto.FeeEstimationRequest;
import org.tbk.bitcoin.tool.fee.bitcore.proto.FeeEstimationResponse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class BitcoreFeeApiClientImplTest {
    private static final String BASE_URL = "https://api.bitcore.io";
    private static final String API_TOKEN = null;

    private BitcoreFeeApiClientImpl sut;

    @BeforeEach
    void setUp() {
        this.sut = new BitcoreFeeApiClientImpl(BASE_URL, API_TOKEN);
    }

    @Test
    void itShouldGetBitcoinMainnetFeeBlock1() {
        FeeEstimationRequest request = FeeEstimationRequest.newBuilder().setBlocks(1).build();
        FeeEstimationResponse response = this.sut.bitcoinMainnetFee(request);

        assertThat(response, is(notNullValue()));
        assertThat(response.getBlocks(), is(greaterThanOrEqualTo(1L))); // sometimes "2" is returned
        assertThat(response.getFeerate(), is(greaterThanOrEqualTo(0d)));
    }

    @Test
    void itShouldGetBitcoinMainnetFeeBlock6() {
        FeeEstimationRequest request = FeeEstimationRequest.newBuilder().setBlocks(6).build();
        FeeEstimationResponse response = this.sut.bitcoinMainnetFee(request);

        assertThat(response, is(notNullValue()));
        assertThat(response.getBlocks(), is(6L));
        assertThat(response.getFeerate(), is(greaterThanOrEqualTo(0d)));
    }

    @Test
    void itShouldGetBitcoinMainnetFeeBlock100() {
        FeeEstimationRequest request = FeeEstimationRequest.newBuilder().setBlocks(100).build();
        FeeEstimationResponse response = this.sut.bitcoinMainnetFee(request);

        assertThat(response, is(notNullValue()));
        assertThat(response.getBlocks(), is(100L));
        assertThat(response.getFeerate(), is(greaterThanOrEqualTo(0d)));
    }
}

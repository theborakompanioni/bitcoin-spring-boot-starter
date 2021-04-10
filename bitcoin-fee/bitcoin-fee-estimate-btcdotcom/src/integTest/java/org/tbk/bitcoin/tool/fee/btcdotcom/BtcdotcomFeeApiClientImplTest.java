package org.tbk.bitcoin.tool.fee.btcdotcom;

import com.google.protobuf.ListValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class BtcdotcomFeeApiClientImplTest {
    private static final String BASE_URL = "https://btc.com";
    private static final String API_TOKEN = null;

    private BtcdotcomFeeApiClientImpl sut;

    @BeforeEach
    public void setUp() {
        this.sut = new BtcdotcomFeeApiClientImpl(BASE_URL, API_TOKEN);
    }

    @Test
    public void itShouldGetFeeDistribution() {
        FeeDistribution feeDistribution = this.sut.feeDistribution();

        assertThat(feeDistribution, is(notNullValue()));
        assertThat(feeDistribution.getTxSizeList(), hasSize(greaterThan(0)));
        assertThat(feeDistribution.getTxSizeCountList(), hasSize(greaterThan(0)));
        assertThat(feeDistribution.getTxSizeDivideMaxSizeList(), hasSize(greaterThan(0)));
        assertThat(feeDistribution.getTxSizeDivideMaxSizeList(), hasSize(greaterThan(0)));

        List<ListValue> txDurationTimeRateList = feeDistribution.getTxDurationTimeRateList();
        assertThat(txDurationTimeRateList, hasSize(greaterThan(0)));

        ListValue firstDurationTimeRate = txDurationTimeRateList.get(0);
        assertThat(firstDurationTimeRate.getValuesList(), hasSize(greaterThan(0)));

        assertThat(feeDistribution.getFeesRecommended(), is(notNullValue()));
        assertThat(feeDistribution.getFeesRecommended().getOneBlockFee(), is(greaterThanOrEqualTo(0L)));

        assertThat(feeDistribution.getUpdateTime(), is(notNullValue()));
    }
}

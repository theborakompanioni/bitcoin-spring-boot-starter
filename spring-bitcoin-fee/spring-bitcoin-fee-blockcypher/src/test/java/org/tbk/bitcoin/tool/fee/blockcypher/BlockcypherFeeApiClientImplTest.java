package org.tbk.bitcoin.tool.fee.blockcypher;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class BlockcypherFeeApiClientImplTest {
    private static final String BASE_URL = "https://api.blockcypher.com";
    private static final String API_TOKEN = null;

    private BlockcypherFeeApiClientImpl sut;

    @Before
    public void setUp() {
        this.sut = new BlockcypherFeeApiClientImpl(BASE_URL, API_TOKEN);
    }

    @Test
    public void itShouldGetBtcMainnet() {
        ChainInfo chainInfo = this.sut.btcMain();

        assertThat(chainInfo, is(notNullValue()));
        assertThat(chainInfo.getName(), is("BTC.main"));
        assertThat(chainInfo.getHeight(), is(greaterThanOrEqualTo(0L)));
        assertThat(chainInfo.getHash(), is(notNullValue()));
        assertThat(chainInfo.getTime(), is(notNullValue()));
        assertThat(chainInfo.getLatestUrl(), startsWith("https://api.blockcypher.com/v1/btc/main/"));
        assertThat(chainInfo.getPreviousHash(), is(notNullValue()));
        assertThat(chainInfo.getPreviousUrl(), startsWith("https://api.blockcypher.com/v1/btc/main/"));
        assertThat(chainInfo.getPeerCount(), is(greaterThanOrEqualTo(0L)));
        assertThat(chainInfo.getUnconfirmedCount(), is(greaterThanOrEqualTo(0L)));
        assertThat(chainInfo.getHighFeePerKb(), is(greaterThanOrEqualTo(0L)));
        assertThat(chainInfo.getLowFeePerKb(), is(greaterThanOrEqualTo(0L)));
        assertThat(chainInfo.getLastForkHeight(), is(greaterThanOrEqualTo(0L)));
        assertThat(chainInfo.getLastForkHash(), is(notNullValue()));
    }

    @Test
    public void itShouldGetBtcTestnet() {
        ChainInfo chainInfo = this.sut.btcTestnet3();

        assertThat(chainInfo, is(notNullValue()));
        assertThat(chainInfo.getName(), is("BTC.test3"));
        assertThat(chainInfo.getHeight(), is(greaterThanOrEqualTo(0L)));
        assertThat(chainInfo.getHash(), is(notNullValue()));
        assertThat(chainInfo.getTime(), is(notNullValue()));
        assertThat(chainInfo.getLatestUrl(), startsWith("https://api.blockcypher.com/v1/btc/test3/"));
        assertThat(chainInfo.getPreviousHash(), is(notNullValue()));
        assertThat(chainInfo.getPreviousUrl(), startsWith("https://api.blockcypher.com/v1/btc/test3/"));
        assertThat(chainInfo.getPeerCount(), is(greaterThanOrEqualTo(0L)));
        assertThat(chainInfo.getUnconfirmedCount(), is(greaterThanOrEqualTo(0L)));
        assertThat(chainInfo.getHighFeePerKb(), is(greaterThanOrEqualTo(0L)));
        assertThat(chainInfo.getLowFeePerKb(), is(greaterThanOrEqualTo(0L)));
        assertThat(chainInfo.getLastForkHeight(), is(greaterThanOrEqualTo(0L)));
        assertThat(chainInfo.getLastForkHash(), is(notNullValue()));
    }
}

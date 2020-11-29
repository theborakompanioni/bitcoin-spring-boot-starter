package org.tbk.bitcoin.txstats.example.score.bitcoinabuse;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.tbk.bitcoin.tool.btcabuse.CheckResponseDto;
import org.tbk.bitcoin.tool.btcabuse.config.BtcAbuseAutoConfigProperties;

import java.util.List;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
        "org.tbk.spring.testcontainer.neo4j.enabled=false",
        "org.tbk.spring.testcontainer.bitcoind.enabled=false"
})
@ActiveProfiles({"test", "local"})
public class BtcAbuseServiceImplTest {

    @Autowired(required = false)
    private BtcAbuseAutoConfigProperties properties;

    @Autowired(required = false)
    private BtcAbuseServiceImpl sut;

    @Before
    public void setUp() {
        // the test will only be invoked if an api-token is specified
        Assume.assumeThat("properties are present", properties, is(notNullValue()));
        Assume.assumeThat("service is enabled", properties.isEnabled(), is(true));
        Assume.assumeThat("apiToken has been provided", properties.getApiToken(), is(notNullValue()));
    }

    @Test
    public void itShouldFindMetaInfoOfAddress() {
        String addressWithMeta = "1KvuTx8TZ4buoXoh9UaPhA4WhZttFwhtbS";

        List<CheckResponseDto> metaInfoOfAddress = this.sut.findMetaInfoOfAddress(addressWithMeta);
        assertThat(metaInfoOfAddress, hasSize(1));

        CheckResponseDto meta = metaInfoOfAddress.get(0);
        assertThat(meta.getAddress(), is(addressWithMeta));
        assertThat(meta.getCount(), is(greaterThan(0L)));
    }
}

package org.tbk.bitcoin.btcabuse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.tbk.bitcoin.btcabuse.client.BtcAbuseApiClient;
import org.tbk.bitcoin.btcabuse.config.BtcAbuseClientAutoConfigProperties;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = BtcAbuseClientTestApplication.class)
@ActiveProfiles("test")
public class BtcAbuseClientStarterTest {

    @Autowired(required = false)
    private BtcAbuseClientAutoConfigProperties properties;

    @Autowired(required = false)
    private BtcAbuseApiClient client;

    @Test
    public void propertiesPresent() {
        assertThat(properties, is(notNullValue()));

        assertThat(properties.isEnabled(), is(true));
        assertThat(properties.getBaseUrl(), is("http://localhost"));
        assertThat(properties.getApiToken(), is("123456"));
        assertThat(properties.getUserAgent(), is("test-agent"));
    }

    @Test
    public void clientPresent() {
        assertThat(client, is(notNullValue()));
    }
}

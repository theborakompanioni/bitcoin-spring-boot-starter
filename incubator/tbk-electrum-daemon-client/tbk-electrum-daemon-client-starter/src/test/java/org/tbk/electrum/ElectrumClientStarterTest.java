package org.tbk.electrum;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.tbk.electrum.config.ElectrumDaemonJsonrpcClientProperties;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ElectrumClientTestApplication.class)
public class ElectrumClientStarterTest {

    @Autowired(required = false)
    private ElectrumDaemonJsonrpcClientProperties properties;

    @Autowired(required = false)
    private ElectrumClientFactory factory;

    @Autowired(required = false)
    private ElectrumClient client;

    @Test
    public void propertiesPresent() {
        assertThat(properties, is(notNullValue()));

        assertThat(properties.isEnabled(), is(true));
        assertThat(properties.getRpchost(), is("http://localhost"));
        assertThat(properties.getRpcport(), is(123456));
        assertThat(properties.getRpcuser(), is("user"));
        assertThat(properties.getRpcpassword(), is("correct horse battery staple"));
    }

    @Test
    public void factoryPresent() {
        assertThat(factory, is(notNullValue()));
    }

    @Test
    public void clientPresent() {
        assertThat(client, is(notNullValue()));
    }
}

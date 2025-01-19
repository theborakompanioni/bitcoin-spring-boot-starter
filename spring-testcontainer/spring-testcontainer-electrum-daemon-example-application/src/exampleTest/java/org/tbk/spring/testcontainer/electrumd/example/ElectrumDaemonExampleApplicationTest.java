package org.tbk.spring.testcontainer.electrumd.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.tbk.electrum.ElectrumClient;
import org.tbk.electrum.model.SimpleBalance;
import org.tbk.spring.testcontainer.electrumd.ElectrumDaemonContainer;
import org.tbk.spring.testcontainer.test.MoreTestcontainerTestUtil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest
@ActiveProfiles("test")
class ElectrumDaemonExampleApplicationTest {

    @Autowired(required = false)
    private ElectrumDaemonContainer<?> container;

    @Autowired(required = false)
    private ElectrumClient electrumClient;

    @Test
    void contextLoads() {
        assertThat(container, is(notNullValue()));
        assertThat(container.isRunning(), is(true));

        Boolean ranForMinimumDuration = MoreTestcontainerTestUtil.ranForMinimumDuration(container).block();

        assertThat("container ran for the minimum amount of time to be considered healthy", ranForMinimumDuration, is(true));
    }

    @Test
    void clientIsConnected() {
        assertThat(electrumClient.isDaemonConnected(), is(true));

        // triggers a lookup on the server
        assertThat(electrumClient.getAddressBalance(electrumClient.createNewAddress()), is(SimpleBalance.zero()));
    }

}

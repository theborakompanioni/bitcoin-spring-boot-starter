package org.tbk.lightning.playground.example;

import org.consensusj.bitcoin.jsonrpc.BitcoinClient;
import org.junit.jupiter.api.Test;
import org.lightningj.lnd.wrapper.SynchronousLndAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;


@SpringBootTest
@ActiveProfiles("test")
class LnPlaygroundExampleApplicationTest {

    @Autowired(required = false)
    private BitcoinClient bitcoinClient;

    @Autowired(required = false)
    private SynchronousLndAPI lndClient;

    @Test
    void contextLoads() {
        assertThat(bitcoinClient, is(notNullValue()));
        assertThat(lndClient, is(notNullValue()));
    }

}

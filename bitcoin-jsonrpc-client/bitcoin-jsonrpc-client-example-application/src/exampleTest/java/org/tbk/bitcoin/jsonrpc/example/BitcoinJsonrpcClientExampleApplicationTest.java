package org.tbk.bitcoin.jsonrpc.example;

import org.consensusj.bitcoin.jsonrpc.BitcoinClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest
@ActiveProfiles("test")
class BitcoinJsonrpcClientExampleApplicationTest {

    @Autowired(required = false)
    private BitcoinClient bitcoinClient;

    @Test
    void contextLoads() {
        assertThat(bitcoinClient, is(notNullValue()));
    }
}

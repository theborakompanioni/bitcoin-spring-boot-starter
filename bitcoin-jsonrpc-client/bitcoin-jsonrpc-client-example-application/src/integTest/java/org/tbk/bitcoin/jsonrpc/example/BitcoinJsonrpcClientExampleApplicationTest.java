package org.tbk.bitcoin.jsonrpc.example;

import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class BitcoinJsonrpcClientExampleApplicationTest {

    @Autowired(required = false)
    private BitcoinClient bitcoinClient;

    @Test
    public void contextLoads() {
        assertThat(bitcoinClient, is(notNullValue()));
    }
}

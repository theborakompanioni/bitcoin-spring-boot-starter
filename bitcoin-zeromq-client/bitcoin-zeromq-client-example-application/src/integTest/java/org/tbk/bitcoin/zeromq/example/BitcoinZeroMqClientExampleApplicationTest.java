package org.tbk.bitcoin.zeromq.example;

import org.bitcoinj.core.Transaction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.tbk.bitcoin.zeromq.client.MessagePublishService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest
@ActiveProfiles("test")
class BitcoinZeroMqClientExampleApplicationTest {

    @Autowired(required = false)
    private MessagePublishService<Transaction> bitcoinjTransactionPublishService;

    @Test
    void contextLoads() {
        assertThat(bitcoinjTransactionPublishService, is(notNullValue()));
    }

}

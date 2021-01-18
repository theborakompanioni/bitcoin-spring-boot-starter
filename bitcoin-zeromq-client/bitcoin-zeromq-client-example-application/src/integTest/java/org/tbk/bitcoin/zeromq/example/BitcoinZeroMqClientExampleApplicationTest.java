package org.tbk.bitcoin.zeromq.example;

import org.bitcoinj.core.Transaction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.tbk.bitcoin.zeromq.client.MessagePublishService;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class BitcoinZeroMqClientExampleApplicationTest {

    @Autowired(required = false)
    private MessagePublishService<Transaction> bitcoinjTransactionPublishService;

    @Test
    public void contextLoads() {
        assertThat(bitcoinjTransactionPublishService, is(notNullValue()));
    }

}

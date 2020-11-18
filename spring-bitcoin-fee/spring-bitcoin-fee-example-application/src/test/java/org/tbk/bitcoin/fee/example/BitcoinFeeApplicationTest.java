package org.tbk.bitcoin.fee.example;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class BitcoinFeeApplicationTest {

    @Autowired(required = false)
    private ApplicationContext applicationContext;

    @Test
    public void contextLoads() {
        assertThat(applicationContext, is(notNullValue()));
    }

}

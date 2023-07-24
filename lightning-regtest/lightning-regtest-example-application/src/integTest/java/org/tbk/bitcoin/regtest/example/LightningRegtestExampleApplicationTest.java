package org.tbk.bitcoin.regtest.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest
@ActiveProfiles("test")
class LightningRegtestExampleApplicationTest {

    @Autowired(required = false)
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        assertThat(applicationContext, is(notNullValue()));
    }

}

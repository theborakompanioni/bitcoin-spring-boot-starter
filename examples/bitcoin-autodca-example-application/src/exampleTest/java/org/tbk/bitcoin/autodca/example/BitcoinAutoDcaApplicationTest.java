package org.tbk.bitcoin.autodca.example;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
class BitcoinAutoDcaApplicationTest {

    @Autowired(required = false)
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        assertThat(applicationContext, is(notNullValue()));
    }

}

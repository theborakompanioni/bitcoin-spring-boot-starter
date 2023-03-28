package org.tbk.spring.testcontainer.bitcoind.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.tbk.spring.testcontainer.bitcoind.BitcoindContainer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;


@SpringBootTest
@ActiveProfiles("test")
class BitcoindContainerExampleApplicationTest {

    @Autowired(required = false)
    private BitcoindContainer<?> bitcoindContainer;

    @Test
    void contextLoads() {
        assertThat(bitcoindContainer, is(notNullValue()));
        assertThat(bitcoindContainer.isRunning(), is(true));
    }

}

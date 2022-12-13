package org.tbk.spring.testcontainer.bitcoind.example;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;


@SpringBootTest
@ActiveProfiles("test")
class BitcoindContainerExampleApplicationTest {

    @Test
    void contextLoads() {
    }

}

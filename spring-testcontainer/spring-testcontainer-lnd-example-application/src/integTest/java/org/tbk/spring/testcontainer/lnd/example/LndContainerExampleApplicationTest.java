package org.tbk.spring.testcontainer.lnd.example;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;


@SpringBootTest
@ActiveProfiles("test")
public class LndContainerExampleApplicationTest {

    @Test
    public void contextLoads() {
    }

}

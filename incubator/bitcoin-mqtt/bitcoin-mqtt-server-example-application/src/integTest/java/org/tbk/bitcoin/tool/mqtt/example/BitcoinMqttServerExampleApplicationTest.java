package org.tbk.bitcoin.tool.mqtt.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.tbk.bitcoin.tool.mqtt.BitcoinMqttServer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest
@ActiveProfiles("test")
class BitcoinMqttServerExampleApplicationTest {

    @Autowired(required = false)
    private BitcoinMqttServer bitcoinMqttServer;

    @Test
    void contextLoads() {
        assertThat(bitcoinMqttServer, is(notNullValue()));
    }

}
